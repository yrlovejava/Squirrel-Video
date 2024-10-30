package com.squirrel.model.interact.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天消息列表 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatListVO {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 私信列表
     */
    private List<ChatVO> chatVOList;
}
