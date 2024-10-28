package com.squirrel.model.video.dtos;

import com.squirrel.model.common.annotation.IdEncrypt;
import lombok.Data;

/**
 * 分享视频的 dto
 */
@Data
public class ShareVideoDTO {

    /**
     * 被分享的视频id
     */
    @IdEncrypt
    private Long videoId;

    /**
     * 接受者id
     */
    @IdEncrypt
    private Long receiverId;
}
