package com.squirrel.utils;

/**
 * threadLocal 工具类
 */
public class ThreadLocalUtil {

    private final static ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置用户 id
     * @param userId 用户id
     */
    public static void setUserId(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取用户id
     * @return 用户id
     */
    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 删除用户id
     */
    public static void removeUserId() {
        USER_THREAD_LOCAL.remove();
    }
}
