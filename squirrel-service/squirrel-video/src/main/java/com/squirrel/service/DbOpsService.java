package com.squirrel.service;

/**
 * 数据库操作接口
 */
public interface DbOpsService {

    /**
     * 安全的增加一个 int 类型的值
     * @param key 视频id
     * @param num 点赞数
     */
    void addIntSafely(String key, int num);

    void insertIntoMongoDB(Long userId,Long videoId,int type,Object ops);
}
