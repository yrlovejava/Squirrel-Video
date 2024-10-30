package com.squirrel.service.impl;

import com.alibaba.fastjson2.JSON;
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
import com.squirrel.service.DbOpsService;
import com.squirrel.service.FileStorageService;
import com.squirrel.service.VideoUploadService;
import com.squirrel.utils.FileUtil;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 视频上传接口实现类
 */
@Slf4j
@Service
public class VideoUploadServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoUploadService {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DbOpsService dbOpsService;

    /**
     * 发布视频
     * @param dto 视频基本信息
     * @return ResponseResult 发布结果
     */
    @Override
    @Transactional
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
            dto.setTitle("视频");
        }
        // 默认封面
        if (dto.getCoverUrl() == null || dto.getCoverUrl().isEmpty()){
            dto.setCoverUrl("?vframe/jpg/offset/0");
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
            // 5.保存在redis
            // 5.1获取key
            String videoKey = VideoConstant.VIDEO_LIST_KEY + video.getId();
            // 5.2将视频存储在对应videoId下
            stringRedisTemplate.opsForList().leftPush(videoKey,JSON.toJSONString(video));
            // 5.3存储在 userId 下的videoId
            stringRedisTemplate.opsForSet().add(VideoConstant.USER_VIDEO_SET + userId, video.getId().toString());
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
     * 获取视频，每次10个
     * 在发布视频时根据 VIDEO_LIST_KEY + 视频id / 11将视频存入相应的 list 中，这样每个 list 都会有10条视频
     * 在每个用户观看视频时根据 NOW_LIST_ID + userId 键在redis中取出对应 id 的list
     * @param lastVideoId 上一次视频id
     * @return ResponseResult
     */
    @Override
    public ResponseResult videos(Integer lastVideoId) {
        // 1.参数校验
        if (lastVideoId == null) {
            throw new NullParamException();
        }

        // 2.根据 listId 取出对应的视频
        List<String> list = stringRedisTemplate.opsForList().range(VideoConstant.VIDEO_LIST_KEY + lastVideoId, 0, -1);
        if (list == null || list.isEmpty()){
            return ResponseResult.errorResult("到底了");
        }
        List<Video> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            // 将redis中的数据反序列化为对象
            Video video = JSON.parseObject(list.get(i), Video.class);
            // 得到 videoId
            Long videoId = video.getId();
            //获取点赞数
            String likes = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_LIKE_KEY + videoId);
            if (likes == null) {
                // 要是发现redis中likes字段过期
                // 则从数据库中查询数据返回，并同时把此视频所有字段刷新到redis
                likes = dbOpsService.getSumFromDB(videoId).toString();
            }
            // 获取评论数
            String collects = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_COLLECT_KEY + videoId);
            collects = collects == null ? "0" : collects;
            // 获取收藏数
            String comments = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_COMMENT_KEY + videoId);
            comments = comments == null ? "0" : comments;
            // 如果当前key为空，直接从数据库中取
            video.setLikes(Long.parseLong(likes));
            video.setCollects(Long.parseLong(collects));
            video.setComments(Long.parseLong(comments));
            result.add(video);
        }
        return ResponseResult.successResult(result);
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
