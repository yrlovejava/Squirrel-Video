package com.squirrel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.model.common.dtos.ResponseResult;
import com.squirrel.model.user.dtos.RegisterDTO;
import com.squirrel.model.user.dtos.UserLoginDTO;
import com.squirrel.model.user.pojos.User;

/**
 * 用户登录操作接口
 */
public interface UserLoginService extends IService<User> {

    /**
     * 用户注册
     * @param dto 注册的 dto
     * @return ResponseResult
     */
    ResponseResult register(RegisterDTO dto);

    /**
     * 用户登录
     * @param dto 登录的 dot
     * @return ResponseResult
     */
    ResponseResult login(UserLoginDTO dto);
}
