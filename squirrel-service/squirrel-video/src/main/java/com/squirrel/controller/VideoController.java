package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.dtos.VideoPublishDTO;
import com.squirrel.service.VideoDoLikeService;
import com.squirrel.service.VideoUploadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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

}
