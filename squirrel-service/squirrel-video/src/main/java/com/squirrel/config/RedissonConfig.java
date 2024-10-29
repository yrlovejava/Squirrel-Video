package com.squirrel.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 */
@Configuration
public class RedissonConfig {

    @Value("${redisson.address}")
    private String redissonAddress;

    @Value("${redisson.password}")
    private String password;

    @Value("${redisson.database}")
    private Integer database;

    @Bean
    public RedissonClient redissonClient() {
        // 配置
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redissonAddress) //地址
                .setPassword(password) //密码
                .setDatabase(database); //数据库

        // 创建 RedissonClient 对象
        return Redisson.create(config);
    }
}
