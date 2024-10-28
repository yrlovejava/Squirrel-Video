package com.squirrel.utils;

import java.util.UUID;

/**
 * 文件相关工具类
 */
public class FileUtil {

    /**
     * 获取objectName uuid + suffix
     * @param fileName 文件名(包括后缀)
     * @return objectName
     */
    public static String getObjectName(String fileName) {
        // 获取后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return UUID.randomUUID() + suffix;
    }
}
