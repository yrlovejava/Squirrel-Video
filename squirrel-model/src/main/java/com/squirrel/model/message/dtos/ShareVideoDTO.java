package com.squirrel.model.message.dtos;

import lombok.Data;

/**
 * 分享视频 DTO
 */
@Data
public class ShareVideoDTO {

    /**
     * 被分享的视频id
     */
    private Long videoId;

    /**
     * 接收者id
     */
    private Long receiverId;
}
