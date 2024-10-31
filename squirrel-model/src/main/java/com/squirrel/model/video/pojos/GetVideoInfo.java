package com.squirrel.model.video.pojos;

import lombok.Data;

import java.util.List;

/**
 * 上传视频的实体类
 */
@Data
public class GetVideoInfo {

    /**
     * 视频列表
     */
    List<VideoDetailInfo> videoList;

    /**
     * 总数
     */
    Integer total;

    /**
     * 上一个视频id
     */
    Integer lastVideoId;
}
