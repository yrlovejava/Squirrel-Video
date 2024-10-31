package com.squirrel.model.video.pojos;

import lombok.Data;

import java.util.List;

/**
 * 查询收藏列表和发布列表需要的类
 */
@Data
public class VideoList {

    /**
     * 视频集合
     */
    public List<Video> videoList;

    /**
     * 总数
     */
    public Integer total;
}
