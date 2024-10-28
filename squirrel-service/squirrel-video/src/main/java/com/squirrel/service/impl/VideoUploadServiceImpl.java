package com.squirrel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.constant.VideoConstant;
import com.squirrel.exception.DbOperationException;
import com.squirrel.exception.NullParamException;
import com.squirrel.exception.QiniuException;
import com.squirrel.exception.UserNotLoginException;
import com.squirrel.mapper.VideoMapper;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.dtos.VideoPublishDTO;
import com.squirrel.model.video.pojos.Video;
import com.squirrel.model.video.vos.VideoUploadVO;
import com.squirrel.service.FileStorageService;
import com.squirrel.service.VideoUploadService;
import com.squirrel.utils.FileUtil;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 视频上传接口实现类
 */
@Slf4j
@Service
public class VideoUploadServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoUploadService {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 发布视频
     * @param dto 视频基本信息
     * @return ResponseResult 发布结果
     */
    @Override
    public ResponseResult publish(VideoPublishDTO dto) {
        log.info("发布视频: {}",dto);
        // 1.校验参数
        if (dto == null) {
            throw new NullParamException();
        }
        if (dto.getVideoUrl().isEmpty()){
            return ResponseResult.errorResult("视频地址不能为空");
        }
        // 超出范围，设定为默认值
        if (dto.getSection() > 9 || dto.getSection() < 0) {
            dto.setSection(VideoConstant.SELECTION_HOT);
        }
        // 标题如果为空默认为 "无标题"
        if (dto.getTitle().isEmpty()){
            dto.setTitle("无标题");
        }

        // 2.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        // 由于有网关过滤，一般是不会存在未登录的情况的，但是这里做防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.封装video数据
        Video video = new Video();
        // 属性拷贝
        BeanUtils.copyProperties(dto,video);
        // 设置作者id
        video.setAuthorId(userId);
        // 状态默认为正常
        video.setStatus(VideoConstant.STATUS_LIVE);

        // 4.保存在数据库
        try {
            save(video);
        }catch (Exception e){
            log.error("视频保存失败: {}",e.toString());
            throw new DbOperationException("保存视频信息失败");
        }

        // 5.封装 vo
        VideoUploadVO vo = new VideoUploadVO();
        BeanUtils.copyProperties(video,vo);

        // 6.返回 vo
        return ResponseResult.successResult(vo);
    }

    /**
     * 上传文件
     * @param file 文件
     * @return ResponseResult 上传结果
     */
    @Override
    public ResponseResult upload(MultipartFile file) {
        log.info("文件上传: {}",file);
        // 1.校验参数
        if (file == null) {
            throw new NullParamException();
        }
        // 2.上传文件
        // 文件路径
        String filePath;
        try {
            filePath = fileStorageService.upload(file.getBytes(),getFileName(file));
        }catch (Exception e){
            log.error("文件上传失败: {}",e.toString());
            throw new QiniuException();
        }

        // 3.返回文件路径
        return ResponseResult.successResult(filePath);
    }

    /**
     * 生成文件名 uuid + 文件名 + 后缀
     * @param file 文件
     * @return 文件名
     */
    private String getFileName(MultipartFile file) {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        return FileUtil.getObjectName(originalFilename);
    }
}
