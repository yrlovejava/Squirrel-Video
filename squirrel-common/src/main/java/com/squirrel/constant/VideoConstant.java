package com.squirrel.constant;

/**
 * 视频的常量类
 */
public class VideoConstant {

    //================ SELECTION ===================
    /**
     * 热门
     */
    public static final Integer SELECTION_HOT = 0;
    /**
     * 直播
     */
    public static final Integer SELECTION_LIVE = 1;
    /**
     * 体育
     */
    public static final Integer SELECTION_SPORT = 2;
    /**
     * 游戏
     */
    public static final Integer SELECTION_GAME = 3;
    /**
     * 番剧
     */
    public static final Integer SELECTION_DRAMA = 4;
    /**
     * 知识
     */
    public static final Integer SELECTION_KNOWLEDGE = 5;
    /**
     * 娱乐
     */
    public static final Integer SELECTION_RECREATION = 6;
    /**
     * 美食
     */
    public static final Integer SELECTION_FOOD = 7;
    /**
     * 时尚
     */
    public static final Integer SELECTION_FASHION = 8;
    /**
     * 热点
     */
    public static final Integer SELECTION_HOTSPOT = 9;

    //================== status ======================
    /**
     * 正常
     */
    public static final Integer STATUS_LIVE = 0;
    /**
     * 删除
     */
    public static final Integer STATUS_DELETE = 1;

    //===============以videoId为key的集合====================
    public static final String SET_LIKE_KEY = "video_set_like:";
    public static final String SET_COLLECT_KEY = "video_set_collect:";
    public static final String SET_COMMENT_KEY = "video_set_comment:";

    //===============以userId为key的集合，记录点赞过的视频
    public static final String USER_SET_LIKE_KEY = "user_set_like:";
    public static final String USER_SET_COLLECT_KEY = "user_set_collect:";
    public static final String USER_SET_COMMENT_KEY = "user_set_comment:";

    //===============与videoId对应的video对象==============
    public static final String VIDEO_ID = "video_id:";

    //===============记录user总点赞数的key===================
    public static final String USER_LIKES_SUM = "user_likes_sum:";
    public static final String USER_COLLECT_SUM = "user_collect_sum:";
    public static final String USER_COMMENTS_SUM = "user_comments_sum:";

    //===============记录userId下的所有视频id，以list存储====================
    public static final String USER_VIDEO_SET_LIST = "user_video_list:";

    //=================记录video的点赞数的key====================
    public static final String STRING_LIKE_KEY = "string_like:";
    public static final String STRING_COLLECT_KEY = "string_collect:";
    public static final String STRING_COMMENT_KEY = "string_comment:";
    //=================记录=======================
    public static final Integer LIKE_TYPE = 1;
    public static final Integer COLLECT_TYPE = 2;
    public static final Integer COMMENT_TYPE = 3;

    /**
     * 存视频的链表头
     */
    public static final String USER_VIDEO_LIST_KEY = "user_video_list:";

    /**
     * 存当前用户应该看的 listId 的键
     */
    public static final String NOW_LIST_ID = "now_list_id:";

    /**
     * 最新的一个视频的id
     */
    public static final String LAST_VIDEO_ID = "last_video_id:";
}
