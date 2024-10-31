package com.squirrel.model.video.pojos;

import lombok.Data;

/**
 * 视频详细信息
 */
@Data
public class VideoDetailInfo {

    /**
     * 视频id
     */
    private Long id;

    /**
     * 作者id
     */
    private Long authorId;

    /**
     * 标题
     */
    private String title;

    /**
     * 分区
     */
    private Integer section;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 收藏数
     */
    private Long collects;

    /**
     * 评论数
     */
    private Long comments;

    /**
     * 是否点赞
     */
    private boolean isLiked;

    /**
     * 是否收藏
     */
    private boolean isCollected;

    /**
     * 作者名字
     */
    private String userName;

    /**
     * 作者头像
     */
    private String image;

    /**
     * 创建时间
     */
    private String createTime;
}
