package com.squirrel.model.message.dtos;

import com.squirrel.model.common.annotation.IdEncrypt;
import lombok.Data;

/**
 * 私信列表 dto
 */
@Data
public class MessageListDTO {

    /**
     * 好友id
     */
    @IdEncrypt
    private Long friendId;

    /**
     * 最后一条(时间最久远的)私信id
     */
    @IdEncrypt
    private Long lastMessageId;
}
