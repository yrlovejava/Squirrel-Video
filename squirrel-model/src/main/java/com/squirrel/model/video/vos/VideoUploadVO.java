package com.squirrel.model.video.vos;

import lombok.Data;

/**
 * 视频上传的vo
 */
@Data
public class VideoUploadVO {

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 封面地址
     */
    private String coverUrl;
}
