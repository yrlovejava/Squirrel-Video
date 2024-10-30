package com.squirrel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.dtos.VideoPublishDTO;
import com.squirrel.model.video.pojos.Video;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频上传接口
 */
public interface VideoUploadService extends IService<Video> {

    /**
     * 发布视频
     * @param dto 视频基本信息
     * @return ResponseResult 发布结果
     */
    ResponseResult publish(VideoPublishDTO dto);

    /**
     * 上传文件
     * @param file 文件
     * @return ResponseResult 上传结果
     */
    ResponseResult upload(MultipartFile file);

    /**
     * 获取视频，每次10个
     * @param lastVideoId 上一次视频id
     * @return ResponseResult
     */
    ResponseResult videos(Integer lastVideoId);

    /**
     * 通过 videoId 得到 video 的实体类
     * @param videoId videoId
     * @return 视频实体类
     */
    Video getVideoById(Integer videoId);
}
