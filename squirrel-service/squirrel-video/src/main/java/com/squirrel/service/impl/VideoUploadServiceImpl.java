package com.squirrel.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.clients.IUserClient;
import com.squirrel.constant.VideoConstant;
import com.squirrel.exception.DbOperationException;
import com.squirrel.exception.NullParamException;
import com.squirrel.exception.QiniuException;
import com.squirrel.exception.UserNotLoginException;
import com.squirrel.mapper.VideoMapper;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.user.vos.UserPersonInfoVO;
import com.squirrel.model.video.dtos.VideoPublishDTO;
import com.squirrel.model.video.pojos.*;
import com.squirrel.model.video.vos.VideoUploadVO;
import com.squirrel.service.DbOpsService;
import com.squirrel.service.FileStorageService;
import com.squirrel.service.VideoUploadService;
import com.squirrel.utils.FileUtil;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private IUserClient userClient;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布视频
     *
     * @param dto 视频基本信息
     * @return ResponseResult 发布结果
     */
    @Override
    @Transactional
    public ResponseResult publish(VideoPublishDTO dto) {
        log.info("发布视频: {}", dto);
        // 1.校验参数
        if (dto == null) {
            throw new NullParamException();
        }
        if (dto.getVideoUrl().isEmpty()) {
            return ResponseResult.errorResult("视频地址不能为空");
        }
        // 超出范围，设定为默认值
        if (dto.getSection() > 9 || dto.getSection() < 0) {
            dto.setSection(VideoConstant.SELECTION_HOT);
        }
        // 标题如果为空默认为 "无标题"
        if (dto.getTitle().isEmpty()) {
            dto.setTitle("视频");
        }
        // 默认封面
        if (dto.getCoverUrl() == null || dto.getCoverUrl().isEmpty()) {
            dto.setCoverUrl("?vframe/jpg/offset/0");
        }
        // 获取当前时间并格式化
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(System.currentTimeMillis());

        // 2.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        // 由于有网关过滤，一般是不会存在未登录的情况的，但是这里做防御性编程
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.封装video数据
        Video video = new Video();
        // 属性拷贝
        BeanUtils.copyProperties(dto, video);
        // 设置作者id
        video.setAuthorId(userId);
        // 状态默认为正常
        video.setStatus(VideoConstant.STATUS_LIVE);
        // 创建时间
        video.setCreateTime(LocalDateTime.now());

        // 4.保存在数据库
        try {
            save(video);
            // 5.保存在redis
            // 5.1获取key
            String videoKey = VideoConstant.VIDEO_LIST_KEY + video.getId();
            // 5.2将视频存储在对应videoId下
            stringRedisTemplate.opsForList().leftPush(videoKey, JSON.toJSONString(video));
            // 5.3存储在 userId 下的videoId
            stringRedisTemplate.opsForList().leftPush(VideoConstant.USER_VIDEO_SET_LIST + userId, video.getId().toString());

            // 6.异步更新ES
            rocketMQTemplate.convertAndSend("video_publish", video);
        } catch (Exception e) {
            log.error("视频保存失败: {}", e.toString());
            throw new DbOperationException("保存视频信息失败");
        }

        // 7.封装 vo
        VideoUploadVO vo = new VideoUploadVO();
        BeanUtils.copyProperties(video, vo);

        // 8.返回 vo
        return ResponseResult.successResult(vo);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return ResponseResult 上传结果
     */
    @Override
    public ResponseResult upload(MultipartFile file) {
        log.info("文件上传: {}", file);
        // 1.校验参数
        if (file == null) {
            throw new NullParamException();
        }
        // 2.上传文件
        // TODO: 视频内容检验
        // 文件路径
        String filePath;
        try {
            filePath = fileStorageService.upload(file.getBytes(), getFileName(file));
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.toString());
            throw new QiniuException();
        }

        // 3.返回文件路径
        return ResponseResult.successResult(filePath);
    }

    /**
     * 获取视频，每次10个
     * 在发布视频时根据 VIDEO_LIST_KEY + 视频id / 11将视频存入相应的 list 中，这样每个 list 都会有10条视频
     * 在每个用户观看视频时根据 NOW_LIST_ID + userId 键在redis中取出对应 id 的list
     *
     * @param lastVideoId 上一次视频id
     * @return ResponseResult
     */
    @Override
    public ResponseResult videos(Integer lastVideoId) {
        // 1.参数校验
        if (lastVideoId == null) {
            throw new NullParamException();
        }

        // 2.查询redis
        GetVideoInfo getVideoInfo = new GetVideoInfo();
        List<VideoDetailInfo> videoList = new ArrayList<>();
        for (int i = lastVideoId * 10 + 1; i < lastVideoId * 10 + 11; i++) {
            //i就是videoId
            //得到video对象
            Video video = getVideoById(i);
            // 如果没有视频了，把之前的视频返回
            if (video == null) {
                getVideoInfo.setVideoList(videoList);
                getVideoInfo.setLastVideoId(i);
                getVideoInfo.setTotal(videoList.size());
                return ResponseResult.successResult(getVideoInfo);
            }
            VideoDetailInfo videoDetailInfo=new VideoDetailInfo();
            BeanUtils.copyProperties(video,videoDetailInfo);
            // 获取时间
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String format = video.getCreateTime().format(df);
            videoDetailInfo.setCreateTime(format);
            // 判断是否喜欢
            videoDetailInfo.setLike(this.isLike((long) i));
            // 得到作者信息
            ResponseResult<UserPersonInfoVO> res = userClient.getUserPersonInfo(video.getAuthorId());
            UserPersonInfoVO user = res.getData();
            videoDetailInfo.setUserName(user.getUsername());
            videoDetailInfo.setImage(user.getImage());
            videoList.add(videoDetailInfo);
        }

        getVideoInfo.setVideoList(videoList);
        getVideoInfo.setLastVideoId(lastVideoId + 10);
        getVideoInfo.setTotal(videoList.size());

        return ResponseResult.successResult(getVideoInfo);
    }

    /**
     * 判断当前用户是否对当前视频进行点赞
     * @param videoId 视频id
     * @return 是否点赞
     */
    private boolean isLike(Long videoId) {
        // 1.获取当前用户
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 2.获取key
        String setLikeKey = VideoConstant.SET_LIKE_KEY + videoId;

        // 3.判断key是否存在
        Boolean hasKey = stringRedisTemplate.hasKey(setLikeKey);
        if (Boolean.TRUE.equals(hasKey)) {
            // 4.如果userId 在 set中，说明已经点过赞了
            Boolean isMember = stringRedisTemplate.opsForSet().isMember(setLikeKey, userId.toString());
            return Boolean.TRUE.equals(isMember);
        }else {
            // 5.如果不存在，那么就从 mongoDB 中获取
            // 5.1查询当前用户id和视频id所在的字段
            Criteria criteria = Criteria.where("userId").is(userId.toString())
                    .and("videoId").is(videoId.toString());
            Query query = Query.query(criteria);
            VideoLike one = mongoTemplate.findOne(query, VideoLike.class);

            // 6.如果此字段不存在，那么就返回未点赞
            return one != null;
        }
    }

    /**
     * 通过videoId得到video的实体类
     *
     * @param videoId videoId
     * @return 视频实体类
     */
    @Override
    public Video getVideoById(Integer videoId) {
        // 1.参数校验
        if (videoId == null) {
            throw new NullParamException();
        }

        // 2.从redis中查询
        // 2.1获取key
        String videoKey = VideoConstant.VIDEO_ID + videoId;
        // 2.2查询
        String videoJson = stringRedisTemplate.opsForValue().get(videoKey + videoId);
        if (videoJson == null) {
            return null;
        }

        // 3.将redis中的json字符串反序列化为对象
        Video video = JSON.parseObject(videoJson, Video.class);
        // 获取点赞数，评论数，收藏数
        String likeStr = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_LIKE_KEY + videoId);
        if (likeStr == null) {
            // 要是发现 redis 中 like 字段过期
            // 则从数据库中查询数据返回，并把此视频所有字段刷新到redis
            likeStr = String.valueOf(dbOpsService.getSumFromDB(videoId.longValue()));
        }
        String collects = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_COLLECT_KEY + videoId);
        String comments = stringRedisTemplate.opsForValue().get(VideoConstant.STRING_COMMENT_KEY + videoId);
        video.setLikes(Long.parseLong(likeStr));
        video.setCollects(collects == null ? 0 : Long.parseLong(collects));
        video.setComments(comments == null ? 0 : Long.parseLong(comments));

        return video;
    }

    /**
     * 生成文件名 uuid + 文件名 + 后缀
     *
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
