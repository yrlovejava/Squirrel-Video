package com.squirrel.model.message.vos;

import com.squirrel.model.common.annotation.IdEncrypt;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私信 vo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    /**
     * 发送者
     */
    private UserPersonInfoBO sender;

    /**
     * 接收者
     */
    private UserPersonInfoBO receiver;

    /**
     * 私信id
     */
    @IdEncrypt
    private Long messageId;

    /**
     * 私信内容
     */
    private String messageContent;

    /**
     * 发送时间
     */
    private LocalDateTime createTime;
}
