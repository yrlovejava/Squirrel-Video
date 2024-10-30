package com.squirrel;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.squirrel.constant.UserConstant;
import com.squirrel.mapper.UserMapper;
import com.squirrel.model.user.pojos.User;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Random;

@SpringBootTest
public class UserInfoTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserMapper userMapper;


    @Test
    public void addInfos() {
        for(int i = 0;i < 100;i++){
            User user = new User();
            UserPersonInfoVO vo = new UserPersonInfoVO();
            user.setUsername(UUID.randomUUID().toString().substring(0,10));
            Random random = new Random();
            String prefix = "1" + (random.nextInt(7) + 3); // 生成手机号前两位
            StringBuilder suffix = new StringBuilder();
            for (int j = 0; j < 9; j++) {
                suffix.append(random.nextInt(10)); // 生成后9位
            }
            String phone = prefix + suffix;
            user.setPhone(phone);
            user.setPassword("dasdsadasdsasadasdsad");
            user.setImage("www.adadasdsa.com");
            user.setSignature("hhhhhh");
            userMapper.insert(user);
            BeanUtils.copyProperties(user,vo);
            vo.setId(user.getId().toString());
            stringRedisTemplate.opsForValue().set(UserConstant.REDIS_USER_INFO + user.getId(), JSON.toJSONString(vo));
        }
    }
}
