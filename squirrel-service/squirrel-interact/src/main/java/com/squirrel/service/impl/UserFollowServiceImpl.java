package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.constant.InteractConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.UserFollowMapper;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.follow.dtos.UserFollowDTO;
import com.squirrel.model.follow.pojos.Follow;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.UserFollowService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 关注接口的实现类
 */
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, Follow> implements UserFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
     * 是否相关关注
     * @param dto 是否相互关注的 dto
     * @return ResponseResult 是否相互关注
     */
    @Override
    public ResponseResult<Boolean> isFollowEachOther(FollowEachOtherDTO dto) {
        // 1.参数校验
        if (dto == null || dto.getFirstUserId() == null || dto.getSecondUserId() == null) {
            throw new NullParamException();
        }

        // 2.查询数据库
        boolean isFollowEachOther = false;
        try {
            Long count1 = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                    .eq(Follow::getFollowId, dto.getFirstUserId()) // 第一个用户被关注
                    .eq(Follow::getUserId, dto.getSecondUserId()) // 第二个用户关注
            );
            Long count2 = getBaseMapper().selectCount(Wrappers.<Follow>lambdaQuery()
                    .eq(Follow::getFollowId, dto.getSecondUserId()) // 第二个用户被关注
                    .eq(Follow::getUserId, dto.getFirstUserId()) // 第一个用户关注
            );
            if (count1 == 1 && count2 == 1){
                isFollowEachOther = true;
            }
        }catch (Exception ex){
            log.error("查询数据库出错: {}",ex.toString());
            throw new DbOperationException("查询数据库出错");
        }

        // 3.返回结果
        return ResponseResult.successResult(isFollowEachOther);
    }

    /**
     * 关注操作
     * @param dto 关注 dto
     * @return ResponseResult 操作结果
     */
    @Override
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

        // 4.获取到锁，执行操作
        try {
            Integer op = dto.getType();
            // 4.1封装 keys 和 values
            List<String> keys = Arrays.asList(followKey,followNumKey,friendKey,targetFollowKey,targetFriendKey);
            Object[] values = new Object[]{userId.toString(),dto.getUserId().toString()};
            if (op.equals(InteractConstant.FOLLOW_CODE)){
                // 关注操作
                // 4.2执行lua脚本
                stringRedisTemplate.execute(FOLLOW_SCRIPT,keys,values);
            }else {
                // 取消关注操作
                // 4.2执行lua脚本
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

        // 5.返回成功
        return ResponseResult.successResult();
    }
}