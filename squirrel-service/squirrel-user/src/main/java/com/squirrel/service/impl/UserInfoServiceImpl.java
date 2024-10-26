package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.squirrel.exception.NullParamException;
import com.squirrel.exception.UserNotExitedException;
import com.squirrel.exception.UserNotLoginException;
import com.squirrel.mapper.UserMapper;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.dtos.UserPersonInfoDTO;
import com.squirrel.model.user.pojos.User;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.UserInfoService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户信息操作接口实现类
 */
@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserMapper userMapper;

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult<UserPersonalInfoVo> 用户个人信息
     */
    @Override
    public ResponseResult<UserPersonInfoVO> getUserPersonInfo(Long userId) {
        // 1.获取到用户的id，如果为空，则说明是查自己，从 ThreadLocal 中获取
        if (userId == null) {
            userId = ThreadLocalUtil.getUserId();
        }
        log.info("用户个人信息查询: {}",userId);

        // 2.校验参数
        if(userId == null) {
            // 说明未登录
            throw new UserNotLoginException();
        }

        // 3.根据用户 id 查询用户信息，只需要 用户名、头像、签名
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getUsername,// 用户名
                User::getImage,// 头像
                User::getSignature// 签名
        );
        User user = userMapper.selectOne(wrapper);

        // 4.校验用户是否为空
        if(user == null) {
            throw new UserNotExitedException();
        }

        // 5.属性拷贝
        UserPersonInfoVO vo = new UserPersonInfoVO();
        BeanUtils.copyProperties(user,vo);

        // 6.返回vo
        return ResponseResult.successResult(vo);
    }

    /**
     * 更新用户个人信息
     * @param dto 用户个人信息
     * @return ResponseResult
     */
    @Override
    public ResponseResult updateUserPersonInfo(UserPersonInfoDTO dto) {
        // 1.校验参数
        if (dto == null) {
            throw new NullParamException();
        }

        // 2.获取用户的id
        if (dto.getId() == null){
            Long userId = ThreadLocalUtil.getUserId();
            if (userId == null) {
                throw new UserNotLoginException();
            }
            dto.setId(userId);
        }
        log.info("用户个人信息更新: {}",dto.getId());

        // 3.属性拷贝
        User user = new User();
        BeanUtils.copyProperties(dto,user);
        userMapper.updateById(user);

        // 4.返回成功
        return ResponseResult.successResult();
    }
}
