package com.squirrel.model.message.vos;

import com.squirrel.model.common.annotation.IdEncrypt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 私信列表 vo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListVO {

    /**
     * 私信总数
     */
    private Integer total;

    /**
     * 最后一条私信id
     */
    @IdEncrypt
    private Long lastMessageId;

    /**
     * 私信列表
     */
    private List<MessageVO> messages;
}
