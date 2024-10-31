package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.dtos.VideoPublishDTO;
import com.squirrel.model.video.pojos.Video;
import com.squirrel.model.video.pojos.VideoList;
import com.squirrel.service.VideoDoLikeService;
import com.squirrel.service.VideoUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * 视频相关功能控制器
 */
@RestController
@RequestMapping("/azaz/video")
public class VideoController {

    @Resource
    private VideoUploadService videoUploadService;

    @Resource
    private VideoDoLikeService videoDoLikeService;

    /**
     * 上传视频
     * @param dto 视频基本信息
     * @return ResponseResult 上传结果
     */
    @PostMapping("/publish")
    public ResponseResult pulish(VideoPublishDTO dto){
        return videoUploadService.publish(dto);
    }

    /**
     * 上传文件
     * @param file 文件
     * @return ResponseResult 上传结果
     */
    @PostMapping("/upload")
    public ResponseResult upload(MultipartFile file){
        return videoUploadService.upload(file);
    }

    /**
     * 点赞操作 type 为1点赞，为0取消点赞
     * @param videoId 视频id
     * @param authorId 作者id
     * @param type 操作类型
     * @return ResponseResult 操作结果
     */
    @PostMapping("/doLike")
    public ResponseResult doLike(Long videoId,Long authorId,int type){
        return videoDoLikeService.like(videoId,authorId,type);
    }

    /**
     * 收藏操作
     * type 为1收藏，为0取消收藏
     * @param videoId 视频id
     * @param authorId 作者id
     * @param type 操作类型
     * @return ResponseResult 操作结果
     */
    @PostMapping("/collect")
    public ResponseResult doCollect(Long videoId,Long authorId,int type){
        return videoDoLikeService.collect(videoId,authorId,type);
    }

    /**
     * 获取视频，每次10个
     * @param lastVideoId 上一次视频id
     * @return ResponseResult
     */
    @GetMapping("/getVideos")
    public ResponseResult getVideo(Integer lastVideoId){
        return videoUploadService.videos(lastVideoId);
    }

    /**
     * 是否点赞
     * @param videoId 视频id
     * @return ResponseResult 是否点赞
     */
    @GetMapping("/isLike")
    public ResponseResult isLike(Long videoId){
        return videoDoLikeService.isLike(videoId);
    }

    /**
     * 得到用户被赞数
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被赞数
     */
    @GetMapping("/getUserLikes")
    public ResponseResult<Integer> getUserLikes(@RequestParam("userId")Long userId){
        return videoDoLikeService.getUserLikes(userId);
    }

    /**
     * 得到用户被收藏数
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被收藏数
     */
    @GetMapping("/getUserCollects")
    public ResponseResult<Integer> getUserCollects(@RequestParam("userId")Long userId) {
        return videoDoLikeService.getUserCollects(userId);
    }

    /**
     * 得到用户发布过的视频
     * @param userId 用户id
     * @return ResponseResult<List<Video>> 用户发布过的所有视频
     */
    @GetMapping("/getAllVideos")
    public ResponseResult<List<Video>> getAllVideos(Long userId) {
        return videoDoLikeService.getAllVideos(userId);
    }

    /**
     * 得到用户点赞过的所有视频
     * @return ResponseResult 点赞过的所有视频
     */
    @GetMapping("/likes")
    public ResponseResult likeVideos() {
        return videoDoLikeService.showLikesList();
    }

    /**
     * 得到用户收藏过的所有视频
     * @param currentPage 当前页
     * @param userId 用户id
     * @return ResponseResult 收藏过的所有视频
     */
    @GetMapping("/showCollectList")
    public ResponseResult<VideoList> collects(Integer currentPage, Integer userId) {
        return videoDoLikeService.showCollectsList(currentPage,userId);
    }

    /**
     * 评论视频
     * @param videoId 视频id
     * @param parentId 关联的评论
     * @param content 评论内容
     * @return ResponseResult 评论操作结果
     */
    @PostMapping("/doComment")
    public ResponseResult doComment(Long videoId,Long parentId,String content) {
        return videoDoLikeService.doComment(videoId,parentId,content);
    }

    /**
     * 得到当前评论的子评论
     * @param commentId 当前评论的id
     * @param videoId 视频id
     * @return ResponseResult 评论集合
     */
    @GetMapping("/getCommentList")
    public ResponseResult doComment(Long commentId,Long videoId) {
        return videoDoLikeService.getCommentList(commentId,videoId);
    }
}
