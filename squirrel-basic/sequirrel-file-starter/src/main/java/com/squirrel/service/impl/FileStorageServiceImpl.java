package com.squirrel.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.squirrel.config.QiniuConfigProperties;
import com.squirrel.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

/**
 * oss 对象存储服务的实现类
 */
@Slf4j
@EnableConfigurationProperties(QiniuConfigProperties.class)
public class FileStorageServiceImpl implements FileStorageService {

    @Resource
    private QiniuConfigProperties qiniuConfigProperties;

    private final static String separator = "/";

    /**
     * 传入文件字符和已经处理过的文件名称(UUID+文件名称+后缀)
     * @param bytes 文件数据
     * @param objectName 文件名
     * @return 七牛云上存储地址
     */
    @Override
    public String upload(byte[] bytes, String objectName) {
        // 文件路径
        String filePath = "";
        Configuration configuration = new Configuration(Zone.autoZone());
        UploadManager manager = new UploadManager(configuration);
        // 生成上传凭证，然后准备上传
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Auth auth = Auth.create(qiniuConfigProperties.getAccessKey(), qiniuConfigProperties.getSecretKey());
        String upToken = auth.uploadToken(qiniuConfigProperties.getBucket());
        try{
            Response response = manager.put(byteArrayInputStream, objectName, upToken, null, null);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            filePath = qiniuConfigProperties.getCDN() + separator + putRet.key;
        }catch (QiniuException ex){
            log.error("{} 七牛云上传失败:{}",objectName,ex.getMessage());
        }
        return filePath;
    }
}
