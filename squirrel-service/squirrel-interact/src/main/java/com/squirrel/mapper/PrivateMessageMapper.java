package com.squirrel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.model.message.pojos.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信 mapper
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 根据用户 id 和好友 id 查询私信列表
     * @param userId 用户id
     * @param friendId 好友id
     * @param lastMessageId 最后一条私信 id
     * @return 私信列表
     */
    List<PrivateMessage> selectByUserIdAndFriendId(@Param("userId")Long userId,@Param("friendId")Long friendId,
                                                   @Param("lastMessageId")Long lastMessageId);
}
