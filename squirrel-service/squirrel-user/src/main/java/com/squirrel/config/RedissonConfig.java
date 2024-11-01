package com.squirrel.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson 配置
 */
@Configuration
public class RedissonConfig {

    @Value("${redisson.address}")
    private String redissonAddress;

    @Value("${redisson.password}")
    private String redissonPassword;

    @Bean
    public RedissonClient redissonClient() {
        // 配置
        Config config = new Config();
        // 地址
        config.useSingleServer().setAddress(redissonAddress).setPassword(redissonPassword);
        // 创建 RedissonClient 对象
        return Redisson.create(config);
    }
}
