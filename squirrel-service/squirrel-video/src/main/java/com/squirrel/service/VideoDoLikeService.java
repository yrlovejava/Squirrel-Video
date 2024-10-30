package com.squirrel.service;

import com.squirrel.model.response.ResponseResult;

public interface VideoDoLikeService {

    /**
     * 点赞操作
     * type 为1点赞 为0取消
     * @param videoId 视频id
     * @param authorId 作者id
     * @param type 操作类型
     * @return ResponseResult 操作结果
     */
    ResponseResult like(Long videoId,Long authorId,int type);

    /**
     * 收藏操作
     * type 为1收藏 为0取消收藏
     * @param videoId 视频id
     * @param authorId 作者id
     * @param type 操作类型
     * @return ResponseResult 操作结果
     */
    ResponseResult collect(Long videoId,Long authorId,int type);

    /**
     * 是否点赞
     * @param videoId 视频id
     * @return ResponseResult 是否点赞
     */
    ResponseResult isLike(Long videoId);

    /**
     * 获取当前用户的点赞数量
     * @return 点赞数量
     */
    Integer getUserLikes();
}
