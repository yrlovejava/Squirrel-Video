package com.squirrel.service.impl;

import com.squirrel.constant.VideoConstant;
import com.squirrel.exception.ErrorParamException;
import com.squirrel.exception.NullParamException;
import com.squirrel.exception.UserNotLoginException;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.DbOpsService;
import com.squirrel.service.VideoDoLikeService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * VideoDoLikeService 实现类
 */
@Slf4j
@Service
public class VideoDoLikeServiceImpl implements VideoDoLikeService {

    @Resource
    DbOpsService dbOpsService;
    @Resource
    StringRedisTemplate stringRedisTemplate;

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

        // 3.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }

        if (type == 1) {
            // 添加到 redis，以 set 方式存储，key 为 videoId，value 为userId
            // 查询用户是否点过赞
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if(Boolean.FALSE.equals(isMember)){
                // 添加到 redis
                this.stringRedisTemplate.opsForSet().add(setKey,String.valueOf(userId));
                // 异步添加到mongoDB
                dbOpsService.insertIntoMongoDB(userId,videoId,VideoConstant.LIKE_TYPE,1);
                // redis数据加一
                dbOpsService.addIntSafely(strKey,1);
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
                this.stringRedisTemplate.opsForSet().remove(setKey,String.valueOf(userId));
                // 异步更新到mongoDB
                dbOpsService.insertIntoMongoDB(userId,videoId,VideoConstant.LIKE_TYPE,0);
                // redis数据减一
                dbOpsService.addIntSafely(strKey,-1);

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

        // 3.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null){
            throw new UserNotLoginException();
        }

        //4.收藏操作
        if(type==1){
            //添加到redis，以set方式存储，key为videoId，value为userId
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if(Boolean.FALSE.equals(isMember)){
                //添加到redis
                this.stringRedisTemplate.opsForSet().add(setKey, String.valueOf(userId));
                //异步添加到mongodb
                dbOpsService.insertIntoMongoDB(userId,videoId, VideoConstant.COLLECT_TYPE,1);
                //redis数据加一
                dbOpsService.addIntSafely(strKey,1);

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
                this.stringRedisTemplate.opsForSet().remove(setKey, userId.toString());
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
}
