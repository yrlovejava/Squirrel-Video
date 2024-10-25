package com.squirrel.service;

/**
 * oss 对象存储服务接口
 */
public interface FileStorageService {

    /**
     * 上传文件到七牛云
     * @param bytes 文件数据
     * @param objectName 文件名
     * @return 七牛云上存储地址
     */
    String upload(byte[] bytes,String objectName);
}
