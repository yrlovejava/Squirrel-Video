


package com.squirrel.controller;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import com.squirrel.model.user.vos.UserHomePageVO;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.UserInfoService;
import jdk.management.resource.ResourceRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * 测试接口
     * @param ids id集合
     * 直接从数据库批量查询，耗时 352ms
     * 循环从redis中查询，耗时 1.34 s
     * @return ResponseResult<List<UserPersonInfoVO>>
     */
    @GetMapping("/personals")
    public ResponseResult<List<UserPersonInfoVO>> getUserPersonInfoList(String[] ids) {
        Set<String> set = new HashSet<>(Arrays.asList(ids));
        return userInfoService.getUserPersonInfos(set);
    }

    /**
     * 更新用户个人信息
     * @param bo 用户个人信息
     * @return 更新结果
     */
    @PutMapping("/personal")
    public ResponseResult updateUserPersonInfo(UserPersonInfoBO bo){
        return userInfoService.updateUserPersonInfo(bo);
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

    /**
     * 获取用户主页信息
     * @param userId 用户id
     * @return ResponseResult<UserHomePageVO> 用户主页信息
     */
    @GetMapping("/homePage")
    public ResponseResult<UserHomePageVO> getUserHomePage(Long userId) {
        return userInfoService.homeUser(userId);
    }
}
