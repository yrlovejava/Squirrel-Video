package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.dtos.RegisterDTO;
import com.squirrel.model.user.dtos.UserLoginDTO;
import com.squirrel.service.UserLoginService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户登录控制器
 */
@RestController
@RequestMapping("/azaz/user/login")
public class UserLoginController {

    @Resource
    private UserLoginService userLoginService;

    /**
     * 用户注册
     * @param dto 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseResult register(RegisterDTO dto){
        return userLoginService.register(dto);
    }

    /**
     * 用户登录
     * @param dto 登录信息
     * @return 登录结果
     */
    @PostMapping()
    public ResponseResult login(UserLoginDTO dto) {
        return userLoginService.login(dto);
    }
}
