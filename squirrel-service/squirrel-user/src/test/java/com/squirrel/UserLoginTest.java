package com.squirrel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
public class UserLoginTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void RedisTest(){
        stringRedisTemplate.opsForValue().set("test","1");
    }
}
