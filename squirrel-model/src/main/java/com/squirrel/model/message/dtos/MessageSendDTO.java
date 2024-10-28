package com.squirrel.model.message.dtos;

import com.squirrel.model.common.annotation.IdEncrypt;
import lombok.Data;

/**
 * 私信发送 id
 */
@Data
public class MessageSendDTO {

    /**
     * 接收者 id
     */
    @IdEncrypt
    private Long receiverId;

    /**
     * 私信内容
     */
    private String content;

    /**
     * 私信类型
     * 0 普通私信
     * 1 朋友分享的视频
     * 2 视频分享
     */
    private Integer status;
}
