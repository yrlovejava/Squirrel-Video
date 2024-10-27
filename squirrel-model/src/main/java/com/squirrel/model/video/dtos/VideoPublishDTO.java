package com.squirrel.model.video.dtos;

import lombok.Data;

/**
 * 发布视频的 dto
 */
@Data
public class VideoPublishDTO {

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 分区
     * 0 热门
     * 1 直播
     * 2 体育
     * 3 游戏
     * 4 番剧
     * 5 知识
     * 6 娱乐
     * 7 美食
     * 8 时尚
     * 9 热点
     */
    private Integer section;
}
