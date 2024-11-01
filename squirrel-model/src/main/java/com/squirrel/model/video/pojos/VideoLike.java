package com.squirrel.model.video.pojos;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@Document("video_like")
public class VideoLike implements Serializable {

    /**
     * 主键
     */
    private String id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 视频id
     */
    private Long videoId;

    /**
     * 是否点赞
     * 1 是
     * 0 否
     */
    private Integer isLike;

    /**
     * 是否收藏
     * 1 是
     * 0 否
     */
    private Integer isCollect;

    /**
     * 评论
     */
    private List<String> commentList;
}
