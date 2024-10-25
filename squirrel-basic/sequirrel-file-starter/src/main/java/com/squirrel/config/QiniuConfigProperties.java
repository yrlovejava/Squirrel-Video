package com.squirrel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 七牛云配置
 */
@Data
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfigProperties {

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
}
