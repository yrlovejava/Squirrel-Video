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
    public String id;

    /**
     * 用户id
     */
    public Long userId;

    /**
     * 视频id
     */
    public Long videoId;

    /**
     * 是否点赞
     * 1 是
     * 0 否
     */
    public Integer isLike;

    /**
     * 是否收藏
     * 1 是
     * 0 否
     */
    public Integer isCollect;

    /**
     * 评论
     */
    public List<String> commentList;
}
