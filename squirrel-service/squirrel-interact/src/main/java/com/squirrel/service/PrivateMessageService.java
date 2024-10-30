package com.squirrel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.model.interact.vos.ChatListVO;
import com.squirrel.model.message.dtos.MessageListDTO;
import com.squirrel.model.message.dtos.MessageSendDTO;
import com.squirrel.model.message.pojos.PrivateMessage;
import com.squirrel.model.message.vos.MessageListVO;
import com.squirrel.model.response.ResponseResult;

/**
 * 私信服务接口
 */
public interface PrivateMessageService extends IService<PrivateMessage> {

    /**
     * 发送私信
     * @param dto 发送私信 dto
     * @return ResponseResult 发送结果
     */
    ResponseResult send(MessageSendDTO dto);

    /**
     * 私信列表
     * @param dto 查询私信列表 dto
     * @return ResponseResult 私信列表
     */
    ResponseResult<MessageListVO> messageList(MessageListDTO dto);

    /**
     * 私信列表
     * @return ResponseResult
     */
    ResponseResult<ChatListVO> chatList();
}
