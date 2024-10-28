package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.clients.IFollowClient;
import com.squirrel.clients.IUserClient;
import com.squirrel.constant.InteractConstant;
import com.squirrel.constant.ResponseConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.PrivateMessageMapper;
import com.squirrel.model.follow.dtos.FollowEachOtherDTO;
import com.squirrel.model.message.dtos.MessageListDTO;
import com.squirrel.model.message.dtos.MessageSendDTO;
import com.squirrel.model.message.pojos.PrivateMessage;
import com.squirrel.model.message.vos.MessageListVO;
import com.squirrel.model.message.vos.MessageVO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.PrivateMessageService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 私信服务接口实现类
 */
@Slf4j
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements PrivateMessageService {

    @Resource
    private IFollowClient followClient;

    @Resource
    private IUserClient userClient;

    /**
     * 发送私信
     * @param dto 发送私信 dto
     * @return ResponseResult 发送结果
     */
    @Override
    public ResponseResult send(MessageSendDTO dto) {
        // 1.校验参数
        if (dto == null || dto.getReceiverId() == null || dto.getContent() == null) {
            throw new NullParamException();
        }
        // 1.2校验私信长度
        if (dto.getContent().length() > InteractConstant.MESSAGE_MAX_LENGTH) {
            throw new ErrorParamException("私信内容不能超过" + InteractConstant.MESSAGE_MAX_LENGTH + "个字符");
        }

        // 2.获取发送者id，也就是当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        // 防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.检查是否互相关注
        // TODO: 从数据查询效率很低，这里需要优化
        FollowEachOtherDTO followEachOtherDTO = new FollowEachOtherDTO();
        followEachOtherDTO.setFirstUserId(userId);
        followEachOtherDTO.setSecondUserId(dto.getReceiverId());
        ResponseResult<Boolean> responseResult = followClient.isFollowEachOther(followEachOtherDTO);
        if(!(responseResult.getCode().equals(ResponseConstant.SUCCESS_CODE))){
            return ResponseResult.errorResult("发送消息失败，内部服务器错误");
        }
        boolean isFollowEachOther = responseResult.getData();

        // 4.如果未互相关注最多只能发送三条私信
        if (!isFollowEachOther){
            // 4.1查询私信条数
            Long count = getBaseMapper().selectCount(Wrappers.<PrivateMessage>lambdaQuery()
                    .eq(PrivateMessage::getReceiverId, dto.getReceiverId())
                    .eq(PrivateMessage::getSenderId, userId)
            );
            // 如果私信条数大于等于 3 条
            if (count >= InteractConstant.MESSAGE_MAX_COUNT){
                throw new ErrorParamException("最多向未互关朋友发送三条私信！");
            }
        }

        // 5.互相关注，或者未互相关注但是发送消息不超过三条，直接发送私信
        // 5.1封装私信
        PrivateMessage privateMessage = PrivateMessage.builder()
                .senderId(userId) //发送者id
                .receiverId(dto.getReceiverId()) // 接收者id
                .messageType(InteractConstant.TYPE_DETAIL) //私信类型
                .status(InteractConstant.STATUS_NORMAL) // 私信状态
                .messageContent(dto.getContent())
                .build();
        // 5.2保存在数据库
        try {
            save(privateMessage);
        }catch (Exception e){
            log.error("私信保存数据库失败: {}",e.toString());
            throw new DbOperationException("私信保存失败");
        }

        // TODO: 保存在redis中

        // 6.返回成功
        return ResponseResult.successResult();
    }

    /**
     * 私信列表
     * @param dto 查询私信列表 dto
     * @return ResponseResult 私信列表
     */
    @Override
    public ResponseResult<MessageListVO> messageList(MessageListDTO dto) {
        // 1.参数校验
        if (dto == null || dto.getFriendId() == null) {
            throw new NullParamException();
        }

        // 2.获取发送者id，也就是当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        // 防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.查询数据库，返回私信列表
        // TODO: 在redis中保存
        return getMessagesInDb(userId, dto.getFriendId(), dto.getLastMessageId());
    }

    /**
     * 从数据库查询失败
     * @param userId 用户id
     * @param friendId 好友id
     * @param lastMessageId 最后一条私信id
     * @return ResponseResult
     */
    private ResponseResult<MessageListVO> getMessagesInDb(Long userId,Long friendId,
                                                          Long lastMessageId) {
        // 1.从数据库中查询私信
        List<PrivateMessage> privateMessageList = getBaseMapper().selectByUserIdAndFriendId(userId, friendId, lastMessageId);

        // 2.封装私信 vo
        List<MessageVO> messageVOList = new ArrayList<>();
        if (privateMessageList == null || privateMessageList.isEmpty()) {
            return ResponseResult.successResult(MessageListVO.builder().messages(messageVOList).build());
        }
        for (PrivateMessage privateMessage : privateMessageList) {
            // 2.1获取发送者信息
            UserPersonInfoBO senderInfo = this.getUserPersonInfo(userId);
            // 2.2获取接收者消息
            UserPersonInfoBO receiverInfo = this.getUserPersonInfo(friendId);
            // 2.3封装私信 vo
            MessageVO messageVO = MessageVO.builder()
                    .messageId(privateMessage.getId())
                    .messageContent(privateMessage.getMessageContent())
                    .sender(senderInfo)
                    .receiver(receiverInfo)
                    .createTime(privateMessage.getCreateTime())
                    .build();
            messageVOList.add(messageVO);
        }

        // 3.更新最后一条私信的id
        lastMessageId = messageVOList.get(messageVOList.size() - 1).getMessageId();

        // 4.封装私信列表 vo
        int total = messageVOList.size();
        MessageListVO messageListVO = MessageListVO.builder()
                .messages(messageVOList)
                .total(total)
                .lastMessageId(lastMessageId)
                .build();

        // 5.返回私信列表 vo
        return ResponseResult.successResult(messageListVO);
    }

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return 用户信息 bo
     */
    private UserPersonInfoBO getUserPersonInfo(Long userId) {
        ResponseResult<UserPersonInfoVO> userPersonInfo = userClient.getUserPersonInfo(userId);
        if (userPersonInfo == null || !userPersonInfo.getCode().equals(ResponseConstant.SUCCESS_CODE)){
            throw new FeignOperationException("获取用户信息的远程调用失败: " + userId);
        }
        UserPersonInfoVO data = userPersonInfo.getData();
        UserPersonInfoBO userPersonInfoBO = UserPersonInfoBO.builder()
                .id(userId)
                .build();
        BeanUtils.copyProperties(data,userPersonInfoBO);
        return userPersonInfoBO;
    }
}
