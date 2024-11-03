package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.clients.IUserClient;
import com.squirrel.constant.InteractConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.UserFollowMapper;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.follow.pojos.Follow;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonalInfoVO;
import com.squirrel.service.UserFollowService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 关注接口的实现类
 */
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, Follow> implements UserFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserClient userClient;

    /**
     * 关注操作的脚本
     */
    private static final DefaultRedisScript<Long> FOLLOW_SCRIPT;

    /**
     * 取消关注的脚本
     */
    private static final DefaultRedisScript<Long> UNFOLLOW_SCRIPT;

    /*
      初始化 lua 脚本
     */
    static {
        FOLLOW_SCRIPT = new DefaultRedisScript<>();
        UNFOLLOW_SCRIPT = new DefaultRedisScript<>();
        FOLLOW_SCRIPT.setLocation(new ClassPathResource("/lua/follow.lua"));
        UNFOLLOW_SCRIPT.setLocation(new ClassPathResource("/lua/unfollow.lua"));
        FOLLOW_SCRIPT.setResultType(Long.class);
        UNFOLLOW_SCRIPT.setResultType(Long.class);
    }

    /**
     * 是否相互关注
     * @param firstUser 第一个用户id
     * @param secondUser 第二个用户id
     * @return ResponseResult<Boolean> 是否相互关注
     */
    @Override
    public ResponseResult<Boolean> isFollowEachOther(Long firstUser,Long secondUser) {
        // 1.参数校验
        if (firstUser == null || secondUser == null) {
            throw new NullParamException();
        }

        // 2.查询redis
        // 2.1获取key
        String firstUserKey = InteractConstant.REDIS_FOLLOW_KEY + firstUser;
        String secondUserKey = InteractConstant.REDIS_FOLLOW_KEY + secondUser;
        boolean isFollowEachOther = false;
        Boolean isFollow = stringRedisTemplate.opsForSet().isMember(firstUserKey, secondUser.toString());
        Boolean isFriend = stringRedisTemplate.opsForSet().isMember(secondUserKey, firstUser.toString());
        if (isFollow == null || isFriend == null) {
            // 3.redis中没有，查询数据库
            try {
                Long count1 = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getFollowId, firstUser) // 第一个用户被关注
                        .eq(Follow::getUserId, secondUser) // 第二个用户关注
                );
                Long count2 = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getFollowId, firstUser) // 第二个用户被关注
                        .eq(Follow::getUserId, secondUser) // 第一个用户关注
                );
                if (count1 == 1 && count2 == 1){
                    isFollowEachOther = true;
                }
            }catch (Exception ex){
                log.error("查询数据库出错: {}",ex.toString());
                throw new DbOperationException("查询数据库出错");
            }
        }else {
            isFollowEachOther = Boolean.TRUE.equals(isFollow) && Boolean.TRUE.equals(isFriend);
        }

        // 4.返回结果
        return ResponseResult.successResult(isFollowEachOther);
    }

    /**
     * 关注操作
     * @param dto 关注 dto
     * @return ResponseResult 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult follow(UserFollowDTO dto) {
        // 1.参数校验
        if (dto == null || dto.getUserId() == null || dto.getType() == null){
            throw new NullParamException();
        }
        if (dto.getType() < 0 || dto.getType() > 1){
            throw new ErrorParamException("操作类型有误");
        }

        // 2.获取当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }
        if (userId.equals(dto.getUserId())){
            throw new ErrorParamException("不能对自己进行此操作!");
        }

        // 3.生成 key
        // 自己关注列表的 key
        String followKey = InteractConstant.REDIS_FOLLOW_KEY + userId;
        // 自己关注数量的 key
        String followNumKey = InteractConstant.REDIS_FOLLOW_NUM_KEY + userId;
        // 自己互关列表的 key
        String friendKey = InteractConstant.REDIS_FRIEND_KEY + userId;
        // 关注对象的关注列表的 key
        String targetFollowKey = InteractConstant.REDIS_FOLLOW_KEY + dto.getUserId();
        // 关注对象的互关列表的 key
        String targetFriendKey = InteractConstant.REDIS_FRIEND_KEY + dto.getUserId();
        // 关注对象的粉丝数量的 key
        String targetFanNumKey = InteractConstant.REDIS_FANS_NUM_KEY + dto.getUserId();

        // 4.获取到锁，执行操作
        try {
            Integer op = dto.getType();
            // 4.1封装 keys 和 values
            List<String> keys = Arrays.asList(followKey,followNumKey,friendKey,targetFollowKey,targetFriendKey,targetFanNumKey);
            Object[] values = new Object[]{userId.toString(),dto.getUserId().toString()};
            if (op.equals(InteractConstant.FOLLOW_CODE)){
                // 关注操作
                // 4.2先写数据库
                Follow follow = Follow.builder()
                        .userId(userId)
                        .followId(dto.getUserId())
                        .build();
                save(follow);
                // 4.3再执行lua脚本
                stringRedisTemplate.execute(FOLLOW_SCRIPT,keys,values);
            }else {
                // 取消关注操作
                // 4.2先在数据库中删除数据
                getBaseMapper().delete(Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getUserId,userId)
                        .eq(Follow::getFollowId,dto.getUserId())
                );
                // 4.3执行再执行lua脚本
                stringRedisTemplate.execute(UNFOLLOW_SCRIPT,keys,values);
            }

        }catch (Exception e){
            if (e instanceof RedisSystemException){
                throw new ErrorOperationException(e.getMessage());
            }else {
                log.error("关注操作执行失败: {}",e.toString());
                throw new LuaExecuteException("关注操作执行失败");
            }
        }

        // 6.返回成功
        return ResponseResult.successResult();
    }

    /**
     * 是否关注
     * @param firstUserId 第一个用户
     * @param secondUserId 第二个用户
     * @return ResponseResult 是否关注
     */
    @Override
    public ResponseResult<Boolean> isFollow(Long firstUserId, Long secondUserId) {
        // 1.参数校验
        if(secondUserId == null){
            throw new NullParamException();
        }
        if (firstUserId == null){
            // 没登陆默认为没关注
            return ResponseResult.successResult(Boolean.FALSE);
        }

        // 2.查询是否关注
        // 2.1获取key
        String friendKey = InteractConstant.REDIS_FRIEND_KEY + firstUserId;
        // 2.2查询redis
        Boolean isFollow = stringRedisTemplate.opsForSet().isMember(friendKey, secondUserId);

        // 3.返回结果
        return ResponseResult.successResult(isFollow);
    }

    /**
     * 获取互关朋友列表
     * @return ResponseResult 互关朋友列表
     */
    @Override
    public ResponseResult<List<UserPersonalInfoVO>> getFriends() {
        // 1.获取当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }

        // 2.查询互关列表
        // 2.1获取key
        String friendKey = InteractConstant.REDIS_FRIEND_KEY + userId;
        // 2.2查询redis
        Set<String> ids = stringRedisTemplate.opsForSet().members(friendKey);
        if(ids == null || ids.isEmpty()){
            return ResponseResult.successResult(Collections.emptyList());
        }

        // 3.远程调用，查询数据库
        return userClient.getUserPersonInfos(ids);
    }

    /**
     * 获取关注列表
     * 现在是全部从redis中查询然后解析，100个用户 耗时大概1s钟
     * 从数据库中直接查询耗时 400多ms
     * 但是这里可以做分页处理，那么从redis遍历查询的性能就差的不多了
     * @return ResponseResult 关注列表
     */
    @Override
    public ResponseResult<List<UserPersonalInfoVO>> getFollowList() {
        // 1.获取当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }

        // 2.查询互关列表
        // 2.1获取key
        String followKey = InteractConstant.REDIS_FOLLOW_KEY + userId;
        // 2.2查询redis
        Set<String> ids = stringRedisTemplate.opsForSet().members(followKey);
        if(ids == null || ids.isEmpty()){
            return ResponseResult.successResult(Collections.emptyList());
        }

        // 3.远程调用，查询关注列表信息
        List<UserPersonalInfoVO> follows = new ArrayList<>();
        for (String id : ids) {
            ResponseResult<UserPersonalInfoVO> userPersonInfo = userClient.getUserPersonInfo(Long.valueOf(id));
            if (userPersonInfo == null || userPersonInfo.getData() == null){
                continue;
            }
            follows.add(userPersonInfo.getData());
        }
        return ResponseResult.successResult(follows);
    }

    /**
     * 获取关注总数
     * @param userId 用户id
     * @return ResponseResult 关注总数
     */
    @Override
    public ResponseResult<Integer> getFollowNum(Long userId) {
        // 1.校验参数
        if (userId == null){
            throw new  NullParamException();
        }

        // 2.查询关注总数
        // 2.1获取key
        String followNumKey = InteractConstant.REDIS_FOLLOW_NUM_KEY + userId;
        // 2.2查询redis
        String numStr = stringRedisTemplate.opsForValue().get(followNumKey);
        if (numStr == null){
            numStr = "0";
        }

        // 3.返回关注总数
        return ResponseResult.successResult(Integer.parseInt(numStr));
    }

    /**
     * 获取粉丝数
     * @param userId 用户id
     * @return ResponseResult 粉丝数
     */
    @Override
    public ResponseResult<Integer> getFansNum(Long userId) {
        // 1.校验参数
        if (userId == null){
            throw new  NullParamException();
        }

        // 2.查询关注总数
        // 2.1获取key
        String fanNumKey = InteractConstant.REDIS_FANS_NUM_KEY + userId;
        // 2.2查询redis
        String numStr = stringRedisTemplate.opsForValue().get(fanNumKey);
        if (numStr == null){
            numStr = "0";
        }

        // 3.返回关注总数
        return ResponseResult.successResult(Integer.parseInt(numStr));
    }
}
