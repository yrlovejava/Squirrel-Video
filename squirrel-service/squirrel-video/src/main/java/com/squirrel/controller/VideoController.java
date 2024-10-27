package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.dtos.VideoPublishDTO;
import com.squirrel.service.VideoUploadService;
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
}
