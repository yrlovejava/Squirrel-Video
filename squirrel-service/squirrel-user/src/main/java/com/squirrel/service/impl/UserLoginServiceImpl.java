package com.squirrel.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.constant.UserConstant;
import com.squirrel.enums.AppHttpCodeEnum;
import com.squirrel.mapper.UserLoginMapper;
import com.squirrel.model.common.dtos.ResponseResult;
import com.squirrel.model.user.dtos.RegisterDTO;
import com.squirrel.model.user.dtos.UserLoginDTO;
import com.squirrel.model.user.pojos.User;
import com.squirrel.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;

/**
 * 用户登录操作接口实现类
 */
@Slf4j
@Service
public class UserLoginServiceImpl extends ServiceImpl<UserLoginMapper, User> implements UserLoginService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     * @param dto 注册的 dto
     * @return ResponseResult
     */
    @Override
    public ResponseResult register(RegisterDTO dto) {
        log.info("用户注册: {}", dto);
        // 1.参数校验
        if (dto == null || dto.getPhone().isEmpty() || dto.getPassword().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"注册信息不能为空");
        }

        // 2.检验手机号是否有效
        String phone = dto.getPhone();
        if (!Validator.isMatchRegex(UserConstant.PHONE_REGEX,phone)){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"手机号不合法");
        }

        // 3.检验密码的长度
        String password = dto.getPassword();
        int len = password.length();
        if(len < UserConstant.PASSWORD_MIN_LENGTH || len > UserConstant.PASSWORD_MAX_LENGTH){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"密码长度不合法");
        }

        // 4.查找手机号是否存在，来判断用户是否注册过
        Long count = getBaseMapper().selectCount(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
        if (count > 0) {
            // 说明手机号已注册过
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"该手机号已经注册过");
        }

        // 5.注册用户
        // 5.1生成 salt 五位的随机字符串
        String salt = RandomUtil.randomString(5);
        // 5.2 password + salt MD5加密
        String passwordWithMD5 = DigestUtils.md5DigestAsHex((password + salt).getBytes());
        // 5.3封装用户
        User user = User.builder()
                .salt(salt)
                .phone(phone)
                .password(passwordWithMD5)
                .username(UserConstant.DEFAULT_USER_NAME_PRE + RandomUtil.randomString(5))
                .image(UserConstant.DEFAULT_USER_IMAGE)
                .signature(UserConstant.DEFAULT_USER_SIGNATURE)
                .build();
        // 5.4向数据库中保存数据
        save(user);

        // 6.返回响应
        return ResponseResult.successResult();
    }

    /**
     * 用户登录
     * @param dto 登录的 dot
     * @return ResponseResult
     */
    @Override
    public ResponseResult login(UserLoginDTO dto) {
        log.info("用户登录: {}",dto);
        // 1.参数校验
        if (dto == null || dto.getPhone().isEmpty() || dto.getPassword().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"注册信息不能为空");
        }

        // 2.检验手机号是否有效
        String phone = dto.getPhone();
        if (!Validator.isMatchRegex(UserConstant.PHONE_REGEX,phone)){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"手机号不合法");
        }

        // 3.检验密码的长度
        String password = dto.getPassword();
        int len = password.length();
        if(len < UserConstant.PASSWORD_MIN_LENGTH || len > UserConstant.PASSWORD_MAX_LENGTH){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"密码长度不合法");
        }

        // 4.在数据库中查询用户
        User user = getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getPhone, phone)
        );
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"该用户不存在，请先注册");
        }

        // 5.校验密码
        String passwordWithMD5 = DigestUtils.md5DigestAsHex((password + user.getSalt()).getBytes());
        if (!passwordWithMD5.equals(user.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR,"密码错误");
        }

        // 6.生成 token 32 位的随机字符串
        String token = RandomUtil.randomString(32);

        // 7.在 redis 中保存 token
        // key : user:login:token:{token}
        // value : {userid}
        // ttl : 两小时
        stringRedisTemplate.opsForValue().set(
                UserConstant.REDIS_LOGIN_TOKEN + token,// key
                user.getId().toString(), // value
                UserConstant.LOGIN_USER_TTL // ttl
        );

        // 8.返回 token
        return ResponseResult.successResult(token);
    }
}
