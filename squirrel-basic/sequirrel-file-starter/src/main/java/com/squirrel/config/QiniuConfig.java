package com.squirrel.config;

import com.squirrel.service.FileStorageService;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Data
@Configuration
// 启用配置文件属性映射的java对象
@EnableConfigurationProperties({QiniuConfigProperties.class})
//当引入FileStorageService接口时,才会装配这个配置类
@ConditionalOnClass(FileStorageService.class)
public class QiniuConfig {

    @Resource
    private QiniuConfigProperties qiniuConfigProperties;

    /**
     * 凭证
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 桶名字
     */
    private String bucket;

    /**
     * CDN
     */
    private String CDN;

    @Bean
    public QiniuConfig qiniuConfig(){
        QiniuConfig qiniuConfig = new QiniuConfig();
        qiniuConfig.setAccessKey(qiniuConfigProperties.getAccessKey());
        qiniuConfig.setSecretKey(qiniuConfigProperties.getSecretKey());
        qiniuConfig.setBucket(qiniuConfigProperties.getBucket());
        qiniuConfig.setCDN(qiniuConfigProperties.getCDN());
        return qiniuConfig;
    }

}
