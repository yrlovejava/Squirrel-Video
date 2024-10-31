package com.squirrel.model.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_comment")
public class Comment {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被评论的视频id
     */
    @TableField("video_id")
    private Long videoId;

    /**
     * 发表评论的用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联的一级评论id，如果是一级评论，则值为0
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 回复的评论id，如果是一级评论则为0
     */
    @TableField("answer_id")
    private Long answerId;

    /**
     * 评论的内容
     */
    @TableField("content")
    private String content;

    /**
     * 点赞数
     */
    @TableField("liked_num")
    private Long likedNum;

    /**
     * 状态
     * 0 正常
     * 1 删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
