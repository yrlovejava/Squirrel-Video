package com.squirrel.model.video.vos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频详细信息
 */
@Data
public class VideoDetail {

    /**
     * 视频id
     */
    private String videoId;

    /**
     * 封面url
     */
    private String coverUrl;

    /**
     * 视频url
     */
    private String videoUrl;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频点赞数
     */
    private Integer likes;

    /**
     * 是否点赞
     */
    private Boolean isLiked;

    /**
     * 是否收藏
     */
    private Boolean isCollected;

    /**
     * 作者id
     */
    private String authorId;

    /**
     * 作者昵称
     */
    private String userName;

    /**
     * 作者头像
     */
    private String image;

    /**
     * 视频发布时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
