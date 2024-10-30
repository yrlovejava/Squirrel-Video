package com.squirrel.model.interact.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatVO {

    /**
     * 用户 id
     */
    private String id;

    /**
     * 对方用户名
     */
    private String username;

    /**
     * 对方头像
     */
    private String image;

    /**
     * 对方签名
     */
    private String signature;

    /**
     * 最后一条消息内容
     */
    private String latestMessage;
}
