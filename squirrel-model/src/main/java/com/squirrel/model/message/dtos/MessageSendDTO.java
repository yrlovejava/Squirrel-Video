package com.squirrel.model.message.dtos;

import lombok.Data;

/**
 * 私信发送 id
 */
@Data
public class MessageSendDTO {

    /**
     * 接收者 id
     */
    private Long receiverId;

    /**
     * 私信内容
     */
    private String content;
}
