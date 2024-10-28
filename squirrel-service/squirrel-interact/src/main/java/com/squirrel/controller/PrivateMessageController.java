package com.squirrel.controller;

import com.squirrel.model.message.dtos.MessageListDTO;
import com.squirrel.model.message.dtos.MessageSendDTO;
import com.squirrel.model.message.vos.MessageListVO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.service.PrivateMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 私信服务控制器
 */
@RestController
@RequestMapping("/azaz/interact/message")
public class PrivateMessageController {

    @Resource
    private PrivateMessageService privateMessageService;

    /**
     * 发送私信
     * @param dto 私信发送 dto
     * @return ResponseResult 发送结果
     */
    @PostMapping("/send")
    public ResponseResult sendPrivateMessage(MessageSendDTO dto){
        return privateMessageService.send(dto);
    }

    /**
     * 私信列表
     * @param dto 查询私信列表的 dto
     * @return ResponseResult 私信列表
     */
    @GetMapping("/list")
    public ResponseResult<MessageListVO> privateMessageList(MessageListDTO dto){
        return privateMessageService.messageList(dto);
    }
}
