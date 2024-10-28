package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.squirrel.constant.UserConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.UserMapper;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import com.squirrel.model.user.pojos.User;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.service.FileStorageService;
import com.squirrel.service.UserInfoService;
import com.squirrel.utils.FileUtil;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 用户信息操作接口实现类
 */
@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private FileStorageService fileStorageService;

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
        wrapper.eq(User::getId, userId);
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
    public ResponseResult updateUserPersonInfo(UserPersonInfoBO dto) {
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

    /**
     * 上传用户头像
     * @param imageFile 用户头像文件
     * @return ResponseResult 图片地址
     */
    @Override
    public ResponseResult<String> uploadImage(MultipartFile imageFile) {
        // 1.校验参数
        if (imageFile == null) {
            throw new NullParamException("头像不能为空");
        }

        // 2.检查是否是支持的文件类型
        String fileName = imageFile.getOriginalFilename();
        if (fileName == null){
            throw new ErrorParamException("不支持的文件类型！");
        }
        // 获取后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(!suffix.equals(UserConstant.IMAGE_TYPE_JPG) && !suffix.equals(UserConstant.IMAGE_TYPE_PNG) && !suffix.equals(UserConstant.IMAGE_TYPE_JPEG)){
            throw new ErrorParamException("不支持的文件类型！");
        }

        // 2.获取用户的id
        Long userId = ThreadLocalUtil.getUserId();
        // 防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.上传头像
        String url;
        try {
            String objectName = FileUtil.getObjectName(fileName);
            url = fileStorageService.upload(imageFile.getBytes(),objectName);
        }catch (Exception e){
            log.error("上传头像失败: {}", e.toString());
            throw new QiniuException("上传头像失败");
        }

        // 4.保存头像地址
        User user = User.builder()
                .id(userId)
                .image(url)
                .build();
        try {
            userMapper.updateById(user);
        }catch (Exception e){
            log.error("修改头像失败: {}", e.toString());
            throw new DbOperationException("修改头像失败");
        }

        // 5.返回图片地址
        return ResponseResult.successResult(url);
    }
}
