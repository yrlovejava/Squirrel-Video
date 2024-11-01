package com.squirrel.service;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.pojos.Video;
import com.squirrel.model.video.pojos.VideoList;
import com.squirrel.model.video.vos.VideoDetail;
import com.squirrel.model.video.vos.VideoInfo;

import java.util.List;

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
     * 获取用户被点赞数量
     * @param userId 用户id
     * @return 点赞数量
     */
    ResponseResult<Integer> getUserLikes(Long userId);

    /**
     * 获取用户被收藏数
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被收藏数
     */
    ResponseResult<Integer> getUserCollects(Long userId);

    /**
     * 获取用户发布过的所有视频
     * @param userId 用户id
     * @return ResponseResult<List<Video>> 用户发布过的所有视频
     */
    ResponseResult<List<Video>> getAllVideos(Long userId);

    /**
     * 得到用户收藏过的所有视频
     * @return ResponseResult 用户收藏过的所有视频
     */
    ResponseResult showLikesList();

    /**
     * 得到用户收藏过的所有视频
     * @param currentPage 当前页
     * @param userId 用户id
     * @return ResponseResult 收藏过的所有视频
     */
    ResponseResult<VideoList> showCollectsList(Integer currentPage, Integer userId);

    /**
     * 获取用户所有作品的数量
     * @param userId 用户id
     * @return ResponseResult<Integer> 作品数
     */
    ResponseResult<Integer> getUserWorks(Long userId);

    /**
     * 评论视频
     * @param videoId 视频id
     * @param parentId 关联的评论
     * @param content 评论内容
     * @return ResponseResult 评论操作结果
     */
    ResponseResult doComment(Long videoId, Long parentId, String content);

    /**
     * 得到当前评论的子评论
     * @param commentId 当前评论的id
     * @param videoId 视频id
     * @return ResponseResult 评论集合
     */
    ResponseResult getCommentList(Long commentId, Long videoId);

}
