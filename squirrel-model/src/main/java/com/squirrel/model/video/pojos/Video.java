package com.squirrel.model.video.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_video")
public class Video {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作者id
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 视频标题
     */
    @TableField("title")
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
    @TableField("section")
    private Integer section;

    /**
     * 封面地址
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 视频地址
     */
    @TableField("video_url")
    private String videoUrl;

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
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
