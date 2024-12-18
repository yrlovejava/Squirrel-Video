package com.squirrel.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.clients.IUserClient;
import com.squirrel.constant.InteractConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.PrivateMessageMapper;
import com.squirrel.model.interact.vos.ChatListVO;
import com.squirrel.model.interact.vos.ChatVO;
import com.squirrel.model.message.dtos.MessageListDTO;
import com.squirrel.model.message.dtos.MessageSendDTO;
import com.squirrel.model.message.pojos.PrivateMessage;
import com.squirrel.model.message.vos.MessageListVO;
import com.squirrel.model.message.vos.MessageVO;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonalInfoVO;
import com.squirrel.service.PrivateMessageService;
import com.squirrel.service.UserFollowService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 私信服务接口实现类
 */
@Slf4j
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements PrivateMessageService {

    @Resource
    private IUserClient userClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private UserFollowService userFollowService;

    /**
     * 发送私信
     * 未优化之前 响应时间 1.59 s
     * 使用 redis 做优化 643 ms
     * @param dto 发送私信 dto
     * @return ResponseResult 发送结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult send(MessageSendDTO dto) {
        log.info("发送私信: {}",dto);
        // 1.校验参数
        if (dto == null || dto.getReceiverId() == null || dto.getContent() == null || dto.getStatus() == null) {
            throw new NullParamException();
        }
        // 1.2校验私信长度
        if (dto.getContent().length() > InteractConstant.MESSAGE_MAX_LENGTH) {
            throw new ErrorParamException("私信内容不能超过" + InteractConstant.MESSAGE_MAX_LENGTH + "个字符");
        }
        // 1.3校验私信类型
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new ErrorParamException("私信类型错误！");
        }

        // 2.获取发送者id，也就是当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        // 防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.检查是否互相关注
        // 3.1从redis中查询
        Boolean isFollowEachOther = userFollowService.isFollowEachOther(userId,dto.getReceiverId()).getData();

        // 4.如果未互相关注最多只能发送三条私信，未关注不能分享视频
        String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(userId, dto.getReceiverId()) + "-" + Math.max(userId, dto.getReceiverId()) + ":";
        if (Boolean.FALSE.equals(isFollowEachOther)){
            // 4.1 判断是否是视频
            if (dto.getStatus().equals(InteractConstant.TYPE_FRIEND_SHARE)){
                throw new ErrorParamException("未相互关注用户不能分享视频！");
            }
            // 4.2查询私信条数
            Long count = stringRedisTemplate.opsForList().size(messageKey);
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
                .status(dto.getStatus()) // 私信状态
                .messageContent(dto.getContent())
                .build();
        // 5.2保存在数据库
        try {
            save(privateMessage);

            // 6.异步操作 redis
            rocketMQTemplate.convertAndSend("private_message",privateMessage);
        }catch (Exception e){
            log.error("私信保存数据库失败: {}",e.toString());
            throw new DbOperationException("私信保存失败");
        }

        // 7.返回成功
        return ResponseResult.successResult();
    }

    /**
     * 私信列表
     * 未使用 redis 存储私信，响应时间 1.42 s
     * 使用 redis 存储 响应时间 59 ms
     * redis 中没有数据的时候 从数据库加载 响应时间 1.69 s
     * @param dto 查询私信列表 dto
     * @return ResponseResult 私信列表
     */
    @Override
    public ResponseResult<MessageListVO> messageList(MessageListDTO dto) {
        log.info("查询私信: {}",dto);
        // 1.参数校验
        if (dto == null || dto.getFriendId() == null) {
            throw new NullParamException();
        }
        if (dto.getLastMessageId() == null) {
            dto.setLastMessageId(0L);
        }

        // 2.获取发送者id，也就是当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        // 防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.从redis中查询私信
        // 3.1生成key "private_message【小id】-【大id】:"
        String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(userId,dto.getFriendId()) + "-" + Math.max(userId, dto.getFriendId()) + ":";
        // 3.2查询redis中是否包含私信key
        Boolean hasKey = stringRedisTemplate.hasKey(messageKey);
        if (hasKey == null || Boolean.FALSE.equals(hasKey) || dto.getLastMessageId() != 0) {
            // redis中没有私信key，从数据库中查询
            return getMessagesInDb(userId,
                    dto.getFriendId(),
                    dto.getLastMessageId(),
                    messageKey);
        }
        // 3.3从redis中查询所有私信
        List<String> messageList = stringRedisTemplate.opsForList().range(messageKey, 0, -1);
        if (messageList == null || messageList.isEmpty()) {
            // 如果redis中没有，查询数据库，并写入redis
            return getMessagesInDb(userId,
                    dto.getFriendId(),
                    dto.getLastMessageId(),
                    messageKey);
        }
        // 4将私信封装为 vo
        List<MessageVO> messageVOList = messageList.stream()
                .map(m -> JSON.parseObject(m, MessageVO.class))
                .collect(Collectors.toList());

        // 5.封装私信列表 vo
        int total = messageVOList.size();
        MessageListVO messageListVO = MessageListVO.builder()
                .total(total)
                .lastMessageId(messageVOList.get(total - 1).getMessageId())
                .messages(messageVOList)
                .build();

        // 6.返回私信列表 vo
        return ResponseResult.successResult(messageListVO);
    }

    /**
     * 从数据库查询私信列表，并保存到redis
     * @param userId 用户id
     * @param friendId 好友id
     * @param lastMessageId 最后一条私信id
     * @param messageKey redis中私信的key
     * @return ResponseResult
     */
    private ResponseResult<MessageListVO> getMessagesInDb(Long userId,Long friendId,
                                                          Long lastMessageId,String messageKey) {
        // 1.从数据库中查询私信
        List<PrivateMessage> privateMessageList = getBaseMapper().selectByUserIdAndFriendId(userId, friendId, lastMessageId);

        // 2.封装私信 vo
        List<MessageVO> messageVOList = new ArrayList<>();
        if (privateMessageList == null || privateMessageList.isEmpty()) {
            return ResponseResult.successResult(MessageListVO.builder().messages(messageVOList).build());
        }
        for (PrivateMessage privateMessage : privateMessageList) {
            // 封装私信 vo
            MessageVO messageVO = MessageVO.builder()
                    .messageId(privateMessage.getId().toString())
                    .messageContent(privateMessage.getMessageContent())
                    .senderId(userId.toString())
                    .receiverId(friendId.toString())
                    .createTime(privateMessage.getCreateTime())
                    .status(privateMessage.getStatus())
                    .build();
            messageVOList.add(messageVO);
        }

        // 3.封装私信列表 vo
        int total = messageVOList.size();
        MessageListVO messageListVO = MessageListVO.builder()
                .messages(messageVOList)
                .total(total)
                .lastMessageId(messageVOList.get(messageVOList.size() - 1).getMessageId())
                .build();

        // 6.在redis中保存
        // 6.1因为现在是从数据查询的时候id降序排列，所以保存到redis的时候需要从右边插入
        List<String> redisMessageList = messageVOList.stream()
                .map(JSON::toJSONString)
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(messageKey,redisMessageList);
        // 6.2设置过期时间
        stringRedisTemplate.expire(messageKey,7, TimeUnit.DAYS);

        // 7.返回私信列表 vo
        return ResponseResult.successResult(messageListVO);
    }

    /**
     * 私信列表
     * @return ResponseResult
     */
    @Override
    public ResponseResult<ChatListVO> chatList() {
        // 1.获取当前用户的id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 2.获取key
        String chatListKey = InteractConstant.REDIS_USER_CHAT_LIST_KEY + userId;

        // 3.查询redis
        List<String> chatList = stringRedisTemplate.opsForList().range(chatListKey, 0, -1);
        if (chatList == null || chatList.isEmpty()) {
            return ResponseResult.successResult(ChatListVO.builder()
                    .chatVOList(Collections.emptyList())
                    .build());
        }

        // 4.查询用户的信息
        List<ChatVO> chatVoList = new ArrayList<>();
        for (String chatUserId : chatList) {
            long otherId = Long.parseLong(chatUserId);
            UserPersonalInfoVO userPersonalInfoVO = userClient.getUserPersonInfo(otherId).getData();
            if (userPersonalInfoVO == null){
                continue;
            }
            // 4.1生成key "private_message【小id】-【大id】:"
            String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(userId, otherId) + "-" + Math.max(userId, otherId) + ":";
            // 4.1获取私信列表的第一条私信
            String message = stringRedisTemplate.opsForList().index(messageKey, 0);
            if (StringUtils.isBlank(message)){
                continue;
            }
            // 4.2封装chatVO
            MessageVO messageVO = JSON.parseObject(message, MessageVO.class);
            ChatVO chatVO = ChatVO.builder()
                    .id(Long.toString(otherId))
                    .username(userPersonalInfoVO.getUsername())
                    .image(userPersonalInfoVO.getImage())
                    .signature(userPersonalInfoVO.getSignature())
                    .latestMessage(messageVO.getMessageContent())
                    .build();
            chatVoList.add(chatVO);
        }

        // 5.封装chatListVO
        ChatListVO chatListVo = ChatListVO.builder()
                .chatVOList(chatVoList)
                .total(chatVoList.size())
                .build();

        // 6.返回VO
        return ResponseResult.successResult(chatListVo);
    }
}
