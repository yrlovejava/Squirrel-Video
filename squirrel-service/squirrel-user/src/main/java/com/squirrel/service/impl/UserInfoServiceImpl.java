package com.squirrel.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.squirrel.clients.IInteractClient;
import com.squirrel.clients.IVideoClient;
import com.squirrel.constant.UserConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.UserMapper;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.bos.UserPersonInfoBO;
import com.squirrel.model.user.dtos.AckPasswordDTO;
import com.squirrel.model.user.pojos.User;
import com.squirrel.model.user.vos.UserHomePageVO;
import com.squirrel.model.user.vos.UserPersonalInfoVO;
import com.squirrel.service.FileStorageService;
import com.squirrel.service.UserInfoService;
import com.squirrel.utils.FileUtil;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IInteractClient interactClient;

    @Resource
    private IVideoClient videoClient;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 获取用户个人信息
     * @param userId 用户id
     * @return ResponseResult<UserPersonalInfoVo> 用户个人信息
     */
    @Override
    public ResponseResult<UserPersonalInfoVO> getUserPersonInfo(Long userId) {
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

        // 3.从 redis 中查询信息
        String userInfoRedis = stringRedisTemplate.opsForValue().get(UserConstant.REDIS_USER_INFO + userId);
        if (userInfoRedis != null){
            // redis 中存在用户信息，直接返回
            UserPersonalInfoVO vo = JSON.parseObject(userInfoRedis, UserPersonalInfoVO.class);
            return ResponseResult.successResult(vo);
        }

        // 5.根据用户 id 查询用户信息，只需要 用户名、头像、签名
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getUsername,// 用户名
                User::getImage,// 头像
                User::getSignature// 签名
        );
        wrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(wrapper);

        // 6.校验用户是否为空
        if(user == null) {
            throw new UserNotExitedException();
        }

        // 7.属性拷贝
        UserPersonalInfoVO vo = new UserPersonalInfoVO();
        BeanUtils.copyProperties(user,vo);
        vo.setId(user.getId().toString());

        // 8.存入redis中
        // 这里存入redis的原因是方便之后会频繁查询user的部分信息，比如每个视频需要用户头像和名字，这时候反复操作数据库数据库压力很大
        // TODO: 测试之后发现在 100个关注对象下，从redis中遍历查询的效率远不如从数据库批量查询，所以这里用户信息的存储需要优化
        // 直接从数据库批量查询，耗时 352ms
        // 循环从redis中查询，耗时 1.34 s
        stringRedisTemplate.opsForValue().set(UserConstant.REDIS_USER_INFO + userId,
                JSON.toJSONString(vo),
                UserConstant.REDIS_USER_INFO_TTL,
                TimeUnit.SECONDS);

        // 9.返回vo
        return ResponseResult.successResult(vo);
    }

    /**
     * 更新用户个人信息
     * @param bo 用户个人信息
     * @return ResponseResult
     */
    @Override
    @Transactional
    public ResponseResult updateUserPersonInfo(UserPersonInfoBO bo) {
        // 1.校验参数
        if (bo == null || bo.getSignature() == null || bo.getImage() == null || bo.getUsername() == null) {
            throw new NullParamException();
        }
        if (bo.getUsername().length() > 15){
            throw new ErrorParamException("用户名不能超过15个字符！");
        }
        if (bo.getSignature().length() > 100){
            throw new ErrorParamException("签名不能超过100个字符！");
        }

        // 2.获取用户的id
        if (bo.getId() == null){
            Long userId = ThreadLocalUtil.getUserId();
            if (userId == null) {
                throw new UserNotLoginException();
            }
            bo.setId(userId);
        }
        log.info("用户个人信息更新: {}",bo.getId());

        // 3.属性拷贝
        User user = new User();
        BeanUtils.copyProperties(bo,user);
        try {
            userMapper.updateById(user);

            // 4.从redis中删除信息
            stringRedisTemplate.delete(UserConstant.REDIS_USER_INFO + bo.getId());
        }catch (Exception e){
            log.error("用户信息更新失败: {}", e.toString());
            throw new ErrorParamException("用户不存在！");
        }

        // 4.发送消息到消息队列，ES索引库中更新用户信息
        rocketMQTemplate.convertAndSend("user_info",user);

        // 5.返回成功
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

    /**
     * 批量获取用户个人信息
     * @param ids 用户id集合
     * @return ResponseResult 个人信息集合
     */
    @Override
    public ResponseResult<List<UserPersonalInfoVO>> getUserPersonInfos(Set<String> ids) {
        // 1.查询数据库
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .select(User::getId, User::getUsername, User::getImage, User::getSignature)
                .in(User::getId, ids)
        );
        if (users == null || users.isEmpty()) {
            return ResponseResult.successResult(Collections.emptyList());
        }

        // 2.封装vo
        List<UserPersonalInfoVO> vos = users.stream().map(u -> {
            UserPersonalInfoVO vo = new UserPersonalInfoVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());

        // 循环查询redis中的数据
//        List<UserPersonInfoVO> vos = new ArrayList<>();
//        for (String id : ids) {
//            String voStr = stringRedisTemplate.opsForValue().get(UserConstant.REDIS_USER_INFO + id);
//            UserPersonInfoVO vo = JSON.parseObject(voStr, UserPersonInfoVO.class);
//            vos.add(vo);
//        }

        // 3.返回vo
        return ResponseResult.successResult(vos);
    }

    /**
     * 获取用户主页信息
     * @param userId 用户id
     * @return ResponseResult<UserHomePageVO> 用户主页信息
     */
    @Override
    public ResponseResult<UserHomePageVO> homeUser(Long userId) {
        // 1.校验参数
        if (userId == null) {
            throw new NullParamException();
        }
        log.info("用户个人信息查询: {}",userId);

        // 2. 获取基本信息
        ResponseResult<UserPersonalInfoVO> userPersonalInfoVoResponseResult = getUserPersonInfo(userId);
        if (userPersonalInfoVoResponseResult == null || userPersonalInfoVoResponseResult.getData() == null){
            throw new UserNotExitedException();
        }
        UserPersonalInfoVO userPersonalInfoVO = userPersonalInfoVoResponseResult.getData();
        // 3.1 获取关注数
        Integer followNum = interactClient.getFollowNum(userId).getData();
        // 3.2 获取粉丝数
        Integer fansNum = interactClient.getFansNum(userId).getData();
        // 3.3 获取点赞数和作品数
        Integer workNums = videoClient.getUserLikes(userId).getData();
        Integer likeNums = videoClient.getUserWorks(userId).getData();
        // 3.4 获取是否关注
        Long curUserId = ThreadLocalUtil.getUserId();
        Boolean isFollow = interactClient.isFollow(curUserId, userId).getData();

        // 4.封装vo
        UserHomePageVO vo = new UserHomePageVO();
        BeanUtils.copyProperties(userPersonalInfoVO,vo);
        vo.setId(userId); // id
        vo.setFollowNum(followNum); // 关注数量
        vo.setFansNum(fansNum); // 粉丝数量
        vo.setLikedNum(likeNums); // 点赞数量
        vo.setWorkNum(workNums); // 作品数量
        vo.setIsFollow(isFollow); // 当前用户是否关注

        // 5.返回 vo
        return ResponseResult.successResult(vo);
    }

    /**
     * 校验密码
     * @param dto 密码dto
     * @return ResponseResult<Boolean> 校验结果
     */
    @Override
    public ResponseResult<Boolean> ackPassword(AckPasswordDTO dto) {
        // 1.参数校验
        if (dto == null || dto.getPassword() == null || dto.getUserId() == null) {
            throw new NullParamException();
        }

        // 2.从数据库中获取用户信息
        User user = userMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new UserNotExitedException();
        }

        // 3.校验密码是否正确
        String md5 = DigestUtils.md5DigestAsHex((dto.getPassword() + user.getSalt()).getBytes());
        if (!md5.equals(user.getPassword())){
            throw new PasswordErrorException();
        }

        return ResponseResult.successResult(Boolean.TRUE);
    }
}
