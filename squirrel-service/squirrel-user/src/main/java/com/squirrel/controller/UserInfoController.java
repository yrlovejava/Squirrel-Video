package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.UserInfoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 用户信息控制器
 */
@RestController
@RequestMapping("/azaz/user")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户个人信息
     *
     * @param  userId 用户id
     * @return ResponseResult<UserPersonalInfoVo> 用户个人信息
     */
    @GetMapping("/personal")
    public ResponseResult<UserPersonInfoVO> getUserPersonInfo(Long userId) {
        return userInfoService.getUserPersonInfo(userId);
    }

    /**
     * 更新用户个人信息
     * @param dto 用户个人信息
     * @return 更新结果
     */
    @PutMapping("/personal")
    public ResponseResult updateUserPersonInfo(UserPersonInfoBO dto){
        return userInfoService.updateUserPersonInfo(dto);
    }

    /**
     * 上传用户头像
     * @param imageFile 用户头像文件
     * @return ResponseResult 图片地址
     */
    @PostMapping("/image/upload")
    public ResponseResult<String> uploadUserImage(MultipartFile imageFile){
        return userInfoService.uploadImage(imageFile);
    }
}
