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
    private String senderId;

    /**
     * 接收者
     */
    private String receiverId;

    /**
     * 私信id
     */
    private String messageId;

    /**
     * 私信内容
     */
    private String messageContent;

    /**
     * 发送时间
     */
    private LocalDateTime createTime;

    /**
     * 消息类型
     * 0 私信
     * 1 朋友分享
     * 2 系统消息
     */
    private Integer status;
}

