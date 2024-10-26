package com.squirrel.service;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.dtos.UserPersonInfoDTO;
import com.squirrel.model.user.vos.UserPersonInfoVO;

/**
 * 用户信息操作接口
 */
public interface UserInfoService {

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult<UserPersonalInfoVo> 用户个人信息
     */
    ResponseResult<UserPersonInfoVO> getUserPersonInfo(Long userId);

    /**
     * 更新用户个人信息
     * @param dto 用户个人信息
     * @return 更新结果
     */
    ResponseResult updateUserPersonInfo(UserPersonInfoDTO dto);
}
