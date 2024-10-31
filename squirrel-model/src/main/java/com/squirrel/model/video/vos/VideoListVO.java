package com.squirrel.model.video.vos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 视频列表 VO
 */
@Data
@Builder
public class VideoListVO {

    /**
     * 本次总数
     */
    private Integer total;

    /**
     * 视频列表
     */
    private List<VideoDetail> videoList;
}
