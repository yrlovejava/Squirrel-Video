package com.squirrel.constant;

/**
 * 互动服务常量类
 */
public class InteractConstant {

    /**
     * 私信内容最大长度
     */
    public static final Integer MESSAGE_MAX_LENGTH = 200;

    /**
     * 未互关朋友最多发送私信数量
     */
    public static final Integer MESSAGE_MAX_COUNT = 3;

    /**
     * 私信redis key
     */
    public static final String REDIS_PRIVATE_MESSAGE_KEY = "private_message";

    /**
     * Redis中保存最多私信数量
     */
    public static final Integer REDIS_PRIVATE_MESSAGE_MAX_COUNT = 30;

    /**
     * 私信正常状态
     */
    public static final Integer STATUS_NORMAL = 0;
    /**
     * 私信删除状态
     */
    public static final Integer STATUS_DELETE = 1;

    /**
     * 私信类型：默认
     */
    public static final Integer TYPE_DETAIL = 0;
    /**
     * 私信类型：朋友分享的视频
     */
    public static final Integer TYPE_FRIEND_SHARE = 1;
    /**
     * 私信类型：系统消息
     */
    public static final Integer TYPE_SYSTEM = 2;

    /**
     * 关注
     */
    public static final Integer FOLLOW_CODE = 1;

    /**
     * 取消关注
     */
    public static final Integer UNFOLLOW_CODE = 0;

    /**
     * 关注总数 redis key
     */
    public static final String REDIS_FOLLOW_NUM_KEY = "follow:num:";

    /**
     * 关注列表redis key
     */
    public static final String REDIS_FOLLOW_KEY = "follow:list:";

    /**
     * 关注锁 redis key
     */
    public static final String REDIS_LOCK_FOLLOW_KEY = "lock::follow:";

    /**
     * 互关列表 redis key
     */
    public static final String REDIS_FRIEND_KEY = "friend:list:";

    /**
     * 粉丝总数 redis key
     */
    public static final String REDIS_FANS_NUM_KEY = "follow:fansNum";

    /**
     * 私信锁redis key
     */
    public static final String REDIS_LOCK_CHAT_KEY = "lock:follow:";

    /**
     * 聊天列表
     */
    public static final String REDIS_USER_CHAT_LIST_KEY = "friend:chatList:";

    /**
     * 聊天列表最大数量
     */
    public static final Long REDIS_USER_CHAT_LIST_MAX_COUNT = 10L;
}
