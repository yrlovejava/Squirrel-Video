package com.squirrel.service.impl;

import com.squirrel.constant.VideoConstant;
import com.squirrel.exception.ErrorParamException;
import com.squirrel.exception.LuaExecuteException;
import com.squirrel.exception.NullParamException;
import com.squirrel.exception.UserNotLoginException;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.pojos.VideoLike;
import com.squirrel.service.DbOpsService;
import com.squirrel.service.VideoDoLikeService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * VideoDoLikeService 实现类
 */
@Slf4j
@Service
public class VideoDoLikeServiceImpl implements VideoDoLikeService {

    @Resource
    private DbOpsService dbOpsService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 点赞操作的lua脚本
     */
    private static final DefaultRedisScript<Long> LIKE_SCRIPT;
    /**
     * 取消点赞操作的lua脚本
     */
    private static final DefaultRedisScript<Long> UNLIKE_SCRIPT;
    /**
     * 关注操作的lua脚本
     */
    private static final DefaultRedisScript<Long> COLLECT_SCRIPT;
    /**
     * 取消关注操作的lua脚本
     */
    private static final DefaultRedisScript<Long> UNCOLLECT_SCRIPT;

    /*
        加载 lua 脚本
     */
    static {
        LIKE_SCRIPT = new DefaultRedisScript<>();
        LIKE_SCRIPT.setResultType(Long.class);
        LIKE_SCRIPT.setLocation(new ClassPathResource("/lua/like.lua"));
        UNLIKE_SCRIPT = new DefaultRedisScript<>();
        UNLIKE_SCRIPT.setResultType(Long.class);
        UNLIKE_SCRIPT.setLocation(new ClassPathResource("/lua/unlike.lua"));
        COLLECT_SCRIPT = new DefaultRedisScript<>();
        COLLECT_SCRIPT.setResultType(Long.class);
        COLLECT_SCRIPT.setLocation(new ClassPathResource("/lua/collect.lua"));
        UNCOLLECT_SCRIPT = new DefaultRedisScript<>();
        UNCOLLECT_SCRIPT.setResultType(Long.class);
        UNCOLLECT_SCRIPT.setLocation(new ClassPathResource("/lua/uncollect.lua"));
    }

    /**
     * 点赞操作
     * type 为1点赞 为0取消
     * @param videoId 视频id
     * @param authorId 作者id
     * @param type 操作类型
     * @return ResponseResult 操作
     */
    @Override
    public ResponseResult like(Long videoId, Long authorId, int type) {
        // 1.校验参数
        if (videoId == null || authorId == null) {
            throw new NullParamException();
        }
        if (type < 0 || type > 1){
            throw new ErrorParamException("操作类型有误");
        }

        // 2.获取所有的key
        // set集合的key
        String setKey = VideoConstant.SET_LIKE_KEY + videoId;
        // kv 类型的 key(key 为 videoId,value 为点赞总数)
        String strKey = VideoConstant.STRING_LIKE_KEY + videoId;
        // 视频作者的点赞总数的 key
        String userKey = VideoConstant.USER_LIKES_SUM + authorId;

        // 3.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }

        // 封装keys
        List<String> keys = Arrays.asList(setKey,strKey,userKey);
        if (type == 1) {
            // 添加到 redis，以 set 方式存储，key 为 videoId，value 为userId
            // 查询用户是否点过赞
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if(Boolean.FALSE.equals(isMember)){
                // 添加到 redis
                // redis数据加一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(LIKE_SCRIPT,keys,userId.toString());
                }catch (Exception e){
                    log.error("lua脚本执行失败: {}",e.toString());
                    throw new LuaExecuteException();
                }
                // 异步添加到mongoDB
                dbOpsService.insertIntoMongoDB(userId,videoId,VideoConstant.LIKE_TYPE,1);
                return ResponseResult.successResult("点赞成功");
            }else {
                return ResponseResult.errorResult("重复点赞");
            }
        }else {
            // 取消点赞
            // 判断是否点过赞
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if(Boolean.TRUE.equals(isMember)){
                // 如果点过赞
                // 删除数据
                // redis数据减一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(UNLIKE_SCRIPT,keys,userId.toString());
                }catch (Exception e){
                    log.error("lua脚本执行失败: {}",e.toString());
                    throw new LuaExecuteException();
                }
                // 异步更新到mongoDB
                dbOpsService.insertIntoMongoDB(userId,videoId,VideoConstant.LIKE_TYPE,0);
                return ResponseResult.successResult();
            }else {
                return ResponseResult.errorResult("重复提交");
            }
        }
    }

    /**
     * 收藏操作
     * type 为1收藏 为0取消收藏
     * @param videoId 视频id
     * @param authorId 作者id
     * @param type 操作类型
     * @return ResponseResult 是否成功
     */
    @Override
    public ResponseResult collect(Long videoId, Long authorId, int type) {
        // 1.校验参数
        if (videoId == null || authorId == null) {
            throw new NullParamException();
        }
        if (type < 0 || type > 1){
            throw new ErrorParamException("操作类型有误");
        }

        // 2.获取所有的key
        // set集合的key
        String setKey = VideoConstant.SET_COLLECT_KEY + videoId;
        // kv 类型的 key(key 为 videoId,value 为点赞总数)
        String strKey = VideoConstant.STRING_COLLECT_KEY + videoId;
        // 视频作者的点赞总数的 key
        String userKey=VideoConstant.USER_COLLECT_SUM+authorId;

        // 3.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }

        //4.收藏操作
        // 封装keys
        List<String> keys = Arrays.asList(setKey,strKey,userKey);
        if(type==1){
            //添加到redis，以set方式存储，key为videoId，value为userId
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if(Boolean.FALSE.equals(isMember)){
                // 添加到redis
                // redis数据加一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(COLLECT_SCRIPT,keys,userId.toString());
                }catch (Exception e){
                    log.error("lua脚本执行失败: {}",e.toString());
                    throw new LuaExecuteException();
                }
                //异步添加到mongodb
                dbOpsService.insertIntoMongoDB(userId,videoId, VideoConstant.COLLECT_TYPE,1);

                return ResponseResult.successResult("收藏成功");
            }
            else {
                return ResponseResult.errorResult("重复收藏");
            }
        }
        //取消收藏
        else {
            //判断是否收藏过
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if(Boolean.TRUE.equals(isMember)){
                //取消收藏
                // 从redis删除
                // redis数据减一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(UNCOLLECT_SCRIPT,keys,userId.toString());
                }catch (Exception e){
                    log.error("lua脚本执行失败: {}",e.toString());
                    throw new LuaExecuteException();
                }
                //异步更新到mongodb
                dbOpsService.insertIntoMongoDB(userId,videoId,VideoConstant.COLLECT_TYPE,0);
                //redis数据减一
                dbOpsService.addIntSafely(strKey,-1);
                return ResponseResult.successResult();
            }
            else {
                return ResponseResult.errorResult("重复取消");
            }
        }

    }

    /**
     * 是否点赞
     * @param videoId 视频id
     * @return ResponseResult 是否点赞
     */
    @Override
    public ResponseResult isLike(Long videoId) {
        // 1.校验参数
        if (videoId == null) {
            throw new NullParamException();
        }

        // 2.获取当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.获取key
        String setLikeKey = VideoConstant.SET_LIKE_KEY + videoId;

        // 4.查询是否存在
        Boolean hasKey = stringRedisTemplate.hasKey(setLikeKey);
        if(Boolean.TRUE.equals(hasKey)){
            // 如果redis中存在
            // 直接在redis中查询
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(setLikeKey, userId.toString());
            if(Boolean.FALSE.equals(isMember)){
                return ResponseResult.successResult(0);
            }
            return ResponseResult.successResult(1);
        }
        // 在redis中不存在，就从mongoDB中查找
        Criteria criteria = Criteria
                .where("userId").is(userId.toString())
                .and("videoId").is(videoId.toString());
        Query query = Query.query(criteria);
        VideoLike one = mongoTemplate.findOne(query, VideoLike.class);
        // 如果此字段不存在，直接返回0
        if (one == null || one.getIsLike() == 0){
            return ResponseResult.successResult(0);
        }
        return ResponseResult.successResult(1);
    }

    /**
     * 获取当前用的的点赞数
     * @return 点赞数
     */
    @Override
    public Integer getUserLikes() {
        // 1.获取当前用户的id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 2.得到对应的key
        String userKey = VideoConstant.USER_LIKES_SUM + userId;

        // 3.查询redis
        String numStr = stringRedisTemplate.opsForValue().get(userKey);
        if (numStr == null) {
            numStr = "0";
        }

        // 4.返回点赞数
        return Integer.parseInt(numStr);
    }
}
