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
    public static final String SET_LIKE_KEY = "set_like:";
    public static final String SET_COLLECT_KEY = "set_collect:";
    public static final String SET_COMMENT_KEY = "set_comment:";

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
    public static final String VIDEO_LIST_KEY = "video_list:";

    /**
     * 存当前用户应该看的 listId 的键
     */
    public static final String NOW_LIST_ID = "now_list_id:";
}
