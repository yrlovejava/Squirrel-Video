package com.squirrel.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileStorageServiceTest {

    static String accessKey = "sVBwiuoG3wB2rVoaISP9rhWQwfHy4eRCsJDxiezN";
    static String secretKey = "l_Dj90KSTiRLipCxqITh5KEURgzVaD9S_8K7CtBo";
    static String bucket = "squirrel-video";

    static String CDN="http://slv79dxvg.hn-bkt.clouddn.com";


    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(upload("Hello World!!!".getBytes(StandardCharsets.UTF_8), "damn.txt"));
    }

    public static String upload(byte[] bytes, String objectName) {
        // 文件路径
        String filePath = "";
        Configuration configuration = new Configuration(Zone.autoZone());
        UploadManager manager = new UploadManager(configuration);
        // 生成上传凭证，然后准备上传
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try{
            Response response = manager.put(byteArrayInputStream, objectName, upToken, null, null);
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            filePath = CDN + "/" + putRet.key;
        }catch (QiniuException ex){
            log.error("{} 七牛云上传失败:{}",objectName,ex.getMessage());
        }
        return filePath;
    }
}
