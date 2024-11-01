package com.squirrel.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.constant.UserConstant;
import com.squirrel.constant.UserDefaultImageConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.UserMapper;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.dtos.RegisterDTO;
import com.squirrel.model.user.dtos.UserLoginDTO;
import com.squirrel.model.user.pojos.User;
import com.squirrel.model.user.vos.UserLoginVO;
import com.squirrel.service.UserLoginService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 用户登录操作接口实现类
 */
@Slf4j
@Service
public class UserLoginServiceImpl extends ServiceImpl<UserMapper, User> implements UserLoginService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 用户注册
     * @param dto 注册的 dto
     * @return ResponseResult
     */
    @Override
    public ResponseResult register(RegisterDTO dto) {
        log.info("用户注册，注册信息: {}", dto);
        // 1.参数校验
        if (dto == null || dto.getPhone().isEmpty() || dto.getPassword().isEmpty()) {
            throw new NullParamException("注册信息不能为空！");
        }

        // 2.检验手机号是否有效
        String phone = dto.getPhone();
        if (!Validator.isMatchRegex(UserConstant.PHONE_REGEX,phone)){
            throw new ErrorParamException("手机号不合法！");
        }

        // 3.检验密码的长度
        String password = dto.getPassword();
        int len = password.length();
        if(len < UserConstant.PASSWORD_MIN_LENGTH || len > UserConstant.PASSWORD_MAX_LENGTH){
            throw new ErrorParamException("密码长度必须在5~20位之间！");
        }

        // 4.查找手机号是否存在，来判断用户是否注册过
        // 对手机号加锁，防止并发注册
        RLock registerLock = redissonClient.getLock(UserConstant.USER_REGISTER_LOCK + phone);
        boolean tryLock = registerLock.tryLock();
        if (!tryLock){
            // 出现并发注册，返回错误信息
            throw new ErrorParamException("注册失败，请稍后再试");
        }
        try {
            Long count = getBaseMapper().selectCount(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
            if (count > 0) {
                // 说明手机号已注册过
                throw new ErrorParamException("手机号已经注册！");
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
                    // 随机生成图像
                    .image(UserDefaultImageConstant.DEFAULT_USER_IMAGE_LIST[RandomUtil.randomInt(UserDefaultImageConstant.IMAGE_COUNT)])
                    .signature(UserConstant.DEFAULT_USER_SIGNATURE)
                    .build();
            // 5.4向数据库中保存数据
            save(user);

            // 6.返回响应
            return ResponseResult.successResult();
        }catch (Exception e){
            log.error("注册失败: {}",e.toString());
            throw new CustomException("注册失败");
        }finally {
            registerLock.unlock();
        }
    }

    /**
     * 用户登录
     * @param dto 登录的 dot
     * @return ResponseResult
     */
    @Override
    public ResponseResult<UserLoginVO> login(UserLoginDTO dto) {
        log.info("用户登录: {}",dto);
        // 1.参数校验
        if (dto == null || dto.getPhone().isEmpty() || dto.getPassword().isEmpty()) {
            throw new NullParamException("登录信息不能为空！");
        }

        // 2.检验手机号是否有效
        String phone = dto.getPhone();
        if (!Validator.isMatchRegex(UserConstant.PHONE_REGEX,phone)){
            throw new ErrorParamException("手机号不合法！");
        }

        // 3.检验密码的长度
        String password = dto.getPassword();
        int len = password.length();
        if(len < UserConstant.PASSWORD_MIN_LENGTH || len > UserConstant.PASSWORD_MAX_LENGTH){
            throw new ErrorParamException("密码错误！");
        }

        // 4.在数据库中查询用户
        User user = getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getPhone, phone)
        );
        if (user == null) {
            throw new UserNotExitedException();
        }

        // 5.校验密码
        String passwordWithMD5 = DigestUtils.md5DigestAsHex((password + user.getSalt()).getBytes());
        if (!passwordWithMD5.equals(user.getPassword())) {
            throw new PasswordErrorException();
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
                UserConstant.LOGIN_USER_TTL, // ttl
                TimeUnit.SECONDS // 时间单位
        );

        // 8.封装 vo
        UserLoginVO vo = new UserLoginVO(token, user.getId().toString());

        // 9.返回 vo
        return ResponseResult.successResult(vo);
    }

    /**
     * 用户登出
     * @return ResponseResult
     */
    @Override
    public ResponseResult logout() {
        // 清除redis中token
        Long userId = ThreadLocalUtil.getUserId();
        String key = UserConstant.REDIS_LOGIN_TOKEN + userId;
        stringRedisTemplate.delete(key);

        return ResponseResult.successResult();
    }
}
