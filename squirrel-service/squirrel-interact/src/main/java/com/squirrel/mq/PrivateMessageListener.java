package com.squirrel.mq;

import com.alibaba.fastjson2.JSON;
import com.squirrel.clients.IUserClient;
import com.squirrel.constant.InteractConstant;
import com.squirrel.model.message.pojos.PrivateMessage;
import com.squirrel.model.message.vos.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 私信消息监听器
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "private_message",consumerGroup = "private_message_group")
public class PrivateMessageListener implements RocketMQListener<PrivateMessage> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserClient userClient;

    @Override
    public void onMessage(PrivateMessage privateMessage) {
        log.info("消费者接收到私信消息: {}",privateMessage);
        // 更新 chat 列表
        Long senderId = privateMessage.getSenderId();
        Long receiverId = privateMessage.getReceiverId();
        // 更新发送者的列表 zset，按照时间排序，越新越靠前
        stringRedisTemplate.opsForZSet().add(InteractConstant.REDIS_USER_CHAT_LIST_KEY + senderId,receiverId.toString(),System.currentTimeMillis());
        // 更新接收者的列表
        stringRedisTemplate.opsForZSet().add(InteractConstant.REDIS_USER_CHAT_LIST_KEY + receiverId,senderId.toString(),System.currentTimeMillis());
        // 将私信转换为 vo 缓存在 redis 中
        // 向 redis 中添加私信，key为小id-大id
        String messageKey = InteractConstant.REDIS_PRIVATE_MESSAGE_KEY + Math.min(senderId, receiverId) + "-" + Math.max(senderId, receiverId) + ":";
        // 封装私信 vo
        MessageVO messageVO = MessageVO.builder()
                .messageId(privateMessage.getId().toString())
                .senderId(senderId.toString())
                .receiverId(receiverId.toString())
                .messageContent(privateMessage.getMessageContent())
                .status(privateMessage.getStatus())
                .createTime(privateMessage.getCreateTime())
                .build();
        // 设置私信过期时间为一周
        stringRedisTemplate.opsForList().leftPush(messageKey, JSON.toJSONString(messageVO));
        stringRedisTemplate.expire(messageKey,7, TimeUnit.DAYS);
        // 检查私信数量是否超过30条，超时则删除最早的一条
        Long size = stringRedisTemplate.opsForList().size(messageKey);
        if (size == null || size > InteractConstant.REDIS_PRIVATE_MESSAGE_MAX_COUNT) {
            stringRedisTemplate.opsForList().rightPop(messageKey);
        }
    }
}
