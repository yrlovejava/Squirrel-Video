package com.squirrel.service;

import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import com.squirrel.model.user.dtos.AckPasswordDTO;
import com.squirrel.model.user.vos.UserHomePageVO;
import com.squirrel.model.user.vos.UserPersonalInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 用户信息操作接口
 */
public interface UserInfoService {

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult<UserPersonalInfoVo> 用户个人信息
     */
    ResponseResult<UserPersonalInfoVO> getUserPersonInfo(Long userId);

    /**
     * 更新用户个人信息
     * @param bo 用户个人信息
     * @return 更新结果
     */
    ResponseResult updateUserPersonInfo(UserPersonInfoBO bo);

    /**
     * 上传用户头像
     * @param imageFile 用户头像文件
     * @return ResponseResult 图片路径
     */
    ResponseResult<String> uploadImage(MultipartFile imageFile);

    /**
     * 批量获取用户个人信息
     * @param ids 用户id集合
     * @return ResponseResult 个人信息集合
     */
    ResponseResult<List<UserPersonalInfoVO>> getUserPersonInfos(Set<String> ids);

    /**
     * 获取用户主页信息
     * @param userId 用户id
     * @return ResponseResult<UserHomePageVO> 用户主页信息
     */
    ResponseResult<UserHomePageVO> homeUser(Long userId);

    /**
     * 校验密码
     * @param dto 密码dto
     * @return ResponseResult<Boolean> 校验结果
     */
    ResponseResult<Boolean> ackPassword(AckPasswordDTO dto);
}
