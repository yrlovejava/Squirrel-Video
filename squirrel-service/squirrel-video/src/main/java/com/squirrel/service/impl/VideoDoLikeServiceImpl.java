package com.squirrel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.squirrel.clients.IUserClient;
import com.squirrel.constant.CommentConstant;
import com.squirrel.constant.VideoConstant;
import com.squirrel.exception.*;
import com.squirrel.mapper.CommentMapper;
import com.squirrel.model.comment.Comment;
import com.squirrel.model.response.ResponseResult;
import com.squirrel.model.video.pojos.Video;
import com.squirrel.model.video.pojos.VideoList;
import com.squirrel.service.DbOpsService;
import com.squirrel.service.VideoDoLikeService;
import com.squirrel.service.VideoUploadService;
import com.squirrel.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * VideoDoLikeService 实现类
 */
@Slf4j
@Service
public class VideoDoLikeServiceImpl implements VideoDoLikeService {

    @Resource
    private DbOpsService dbOpsService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private VideoUploadService videoUploadService;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private IUserClient userClient;

    /**
     * 点赞操作的lua脚本
     */
    private static final DefaultRedisScript<Long> LIKE_SCRIPT;
    /**
     * 取消点赞操作的lua脚本
     */
    private static final DefaultRedisScript<Long> UNLIKE_SCRIPT;
    /**
     * 关注操作的lua脚本
     */
    private static final DefaultRedisScript<Long> COLLECT_SCRIPT;
    /**
     * 取消关注操作的lua脚本
     */
    private static final DefaultRedisScript<Long> UNCOLLECT_SCRIPT;

    /*
        加载 lua 脚本
     */
    static {
        LIKE_SCRIPT = new DefaultRedisScript<>();
        LIKE_SCRIPT.setResultType(Long.class);
        LIKE_SCRIPT.setLocation(new ClassPathResource("/lua/like.lua"));
        UNLIKE_SCRIPT = new DefaultRedisScript<>();
        UNLIKE_SCRIPT.setResultType(Long.class);
        UNLIKE_SCRIPT.setLocation(new ClassPathResource("/lua/unlike.lua"));
        COLLECT_SCRIPT = new DefaultRedisScript<>();
        COLLECT_SCRIPT.setResultType(Long.class);
        COLLECT_SCRIPT.setLocation(new ClassPathResource("/lua/collect.lua"));
        UNCOLLECT_SCRIPT = new DefaultRedisScript<>();
        UNCOLLECT_SCRIPT.setResultType(Long.class);
        UNCOLLECT_SCRIPT.setLocation(new ClassPathResource("/lua/uncollect.lua"));
    }

    /**
     * 点赞操作
     * type 为1点赞 为0取消
     *
     * @param videoId  视频id
     * @param authorId 作者id
     * @param type     操作类型
     * @return ResponseResult 操作
     */
    @Override
    public ResponseResult like(Long videoId, Long authorId, int type) {
        // 1.校验参数
        if (videoId == null || authorId == null) {
            throw new NullParamException();
        }
        if (type < 0 || type > 1) {
            throw new ErrorParamException("操作类型有误");
        }

        // 2.获取所有的key
        // set集合的key
        String setKey = VideoConstant.SET_LIKE_KEY + videoId;
        // kv 类型的 key(key 为 videoId,value 为点赞总数)
        String strKey = VideoConstant.STRING_LIKE_KEY + videoId;
        // 视频作者的点赞总数的 key
        String userKey = VideoConstant.USER_LIKES_SUM + authorId;

        // 3.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }
        // 当前用户点赞的视频集合key
        String nowUserKey = VideoConstant.USER_SET_LIKE_KEY + userId;

        // 封装keys
        List<String> keys = Arrays.asList(setKey, strKey, userKey, nowUserKey);
        if (type == 1) {
            // 添加到 redis，以 set 方式存储，key 为 videoId，value 为userId
            // 查询用户是否点过赞
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if (Boolean.FALSE.equals(isMember)) {
                // 添加到 redis
                // redis数据加一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(LIKE_SCRIPT, keys, userId.toString(), videoId.toString());
                } catch (Exception e) {
                    if (e instanceof RedisSystemException){
                        log.error("重复点赞");
                        throw new ErrorOperationException("请勿重复点赞");
                    }else {
                        log.error("lua脚本执行失败: {}", e.toString());
                        throw new LuaExecuteException("lua脚本执行失败");
                    }
                }
                // 异步添加到mongoDB
                dbOpsService.insertIntoMongoDB(userId, videoId, VideoConstant.LIKE_TYPE, 1);
                return ResponseResult.successResult("点赞成功");
            } else {
                return ResponseResult.errorResult("重复点赞");
            }
        } else {
            // 取消点赞
            // 判断是否点过赞
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if (Boolean.TRUE.equals(isMember)) {
                // 如果点过赞
                // 删除数据
                // redis数据减一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(UNLIKE_SCRIPT, keys, userId.toString(), videoId.toString());
                } catch (Exception e) {
                    if (e instanceof RedisSystemException){
                        log.error("重复取消");
                        throw new ErrorOperationException("请勿重复取消");
                    }else {
                        log.error("lua脚本执行失败: {}", e.toString());
                        throw new LuaExecuteException("lua脚本执行失败");
                    }
                }
                // 异步更新到mongoDB
                dbOpsService.insertIntoMongoDB(userId, videoId, VideoConstant.LIKE_TYPE, 0);
                return ResponseResult.successResult();
            } else {
                return ResponseResult.errorResult("重复取消");
            }
        }
    }

    /**
     * 收藏操作
     * type 为1收藏 为0取消收藏
     *
     * @param videoId  视频id
     * @param authorId 作者id
     * @param type     操作类型
     * @return ResponseResult 是否成功
     */
    @Override
    public ResponseResult collect(Long videoId, Long authorId, int type) {
        // 1.校验参数
        if (videoId == null || authorId == null) {
            throw new NullParamException();
        }
        if (type < 0 || type > 1) {
            throw new ErrorParamException("操作类型有误");
        }

        // 2.获取所有的key
        // set集合的key
        String setKey = VideoConstant.SET_COLLECT_KEY + videoId;
        // kv 类型的 key(key 为 videoId,value 为点赞总数)
        String strKey = VideoConstant.STRING_COLLECT_KEY + videoId;
        // 视频作者的收藏总数的 key
        String userKey = VideoConstant.USER_COLLECT_SUM + authorId;

        // 3.获取用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }
        // 当前用户收藏的视频集合的 key
        String nowUserKey = VideoConstant.USER_SET_COLLECT_KEY + userId;

        //4.收藏操作
        // 封装keys
        List<String> keys = Arrays.asList(setKey, strKey, userKey, nowUserKey);
        if (type == 1) {
            //添加到redis，以set方式存储，key为videoId，value为userId
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if (Boolean.FALSE.equals(isMember)) {
                // 添加到redis
                // redis数据加一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(COLLECT_SCRIPT, keys, userId.toString(), videoId.toString());
                } catch (Exception e) {
                    if (e instanceof RedisSystemException){
                        log.error("重复收藏");
                        throw new ErrorOperationException("请勿重复收藏");
                    }else {
                        log.error("lua脚本执行失败: {}", e.toString());
                        throw new LuaExecuteException("lua脚本执行失败");
                    }
                }
                //异步添加到mongodb
                dbOpsService.insertIntoMongoDB(userId, videoId, VideoConstant.COLLECT_TYPE, 1);
                return ResponseResult.successResult("收藏成功");
            } else {
                return ResponseResult.errorResult("重复收藏");
            }
        }
        //取消收藏
        else {
            //判断是否收藏过
            Boolean isMember = this.stringRedisTemplate.opsForSet().isMember(setKey, String.valueOf(userId));
            if (Boolean.TRUE.equals(isMember)) {
                //取消收藏
                // 从redis删除
                // redis数据减一
                try {
                    // 执行lua脚本
                    this.stringRedisTemplate.execute(UNCOLLECT_SCRIPT, keys, userId.toString(), videoId.toString());
                } catch (Exception e) {
                    if (e instanceof RedisSystemException){
                        log.error("重复取消");
                        throw new ErrorOperationException("请勿重复取消");
                    }else {
                        log.error("lua脚本执行失败: {}", e.toString());
                        throw new LuaExecuteException("lua脚本执行失败");
                    }
                }
                //异步更新到mongodb
                dbOpsService.insertIntoMongoDB(userId, videoId, VideoConstant.COLLECT_TYPE, 0);
                return ResponseResult.successResult();
            } else {
                return ResponseResult.errorResult("重复取消");
            }
        }

    }

    /**
     * 获取用户被的点赞数
     *
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被赞数
     */
    @Override
    public ResponseResult<Integer> getUserLikes(Long userId) {
        // 1.校验参数
        if (userId == null) {
            throw new NullParamException();
        }

        // 2.得到对应的key
        String userKey = VideoConstant.USER_LIKES_SUM + userId;

        // 3.查询redis
        String numStr = stringRedisTemplate.opsForValue().get(userKey);
        if (numStr == null) {
            numStr = "0";
        }

        // 4.返回点赞数
        return ResponseResult.successResult(Integer.parseInt(numStr));
    }

    /**
     * 获取用户被收藏数
     *
     * @param userId 用户id
     * @return ResponseResult<Integer> 用户被收藏数
     */
    @Override
    public ResponseResult<Integer> getUserCollects(Long userId) {
        // 1.校验参数
        if (userId == null) {
            throw new NullParamException();
        }

        // 2.获取key
        String userKey = VideoConstant.USER_COLLECT_SUM + userId;

        // 3.从redis中查询
        String numStr = stringRedisTemplate.opsForValue().get(userKey);
        if (numStr == null) {
            numStr = "0";
        }

        // 4.返回结果
        return ResponseResult.successResult(Integer.parseInt(numStr));
    }

    /**
     * 获取用户发布的所有视频
     * @param currentPage 当前页
     * @param userId 用户id
     * @return ResponseResult<VideoList> 视频列表
     */
    @Override
    public ResponseResult<VideoList> getPublishedVideos(Integer currentPage, Integer userId) {
        // 1.校验参数
        if (currentPage == null || userId == null){
            throw new NullParamException();
        }

        // 2.获取对应的key
        String key = VideoConstant.USER_VIDEO_SET_LIST + userId;

        // 3.得到用户发布的当前页数的videoIds
        List<String> videoIds = stringRedisTemplate.opsForList().range(key, (currentPage - 1) * 10L, currentPage * 10 - 1);
        if (videoIds == null) {
            return ResponseResult.successResult(new VideoList());
        }

        // 4.查询video
        List<Video> videos = new ArrayList<>();
        // 得到 videoId 对应的实体类
        for (String videoId : videoIds) {
            Video video = videoUploadService.getVideoById(Integer.parseInt(videoId));
            if (video != null) {
                videos.add(video);
            }
        }

        // 5.封装返回
        VideoList videoList = new VideoList();
        videoList.setVideoList(videos);
        // 得到视频总数
        Long total = Objects.requireNonNull(stringRedisTemplate.opsForList().size(key));
        videoList.setTotal(total.intValue());

        return ResponseResult.successResult(videoList);
    }


    /**
     * 得到用户收藏过的所有视频
     *
     * @return ResponseResult 收藏过的所有视频
     */
    @Override
    public ResponseResult<VideoList> showCollectsList(Integer currentPage, Integer userId) {
        // 1.参数校验
        if (currentPage == null || userId == null) {
            throw new NullParamException();
        }

        // 2.查询该用户收藏的当前页数的videoId
        // 2.1获取key
        String key = VideoConstant.USER_SET_COLLECT_KEY + userId;
        List<String> videoIds = stringRedisTemplate.opsForList().range(key, (currentPage - 1) * 10L, currentPage * 10 - 1);
        if (videoIds == null || videoIds.isEmpty()) {
            return ResponseResult.successResult(new VideoList());
        }

        // 3.获取video
        List<Video> videos = new ArrayList<>();
        for (String videoId : videoIds) {
            Video video = videoUploadService.getVideoById(Integer.parseInt(videoId));
            if (video != null) {
                videos.add(video);
            }
        }

        // 4.封装vo
        VideoList videoList = new VideoList();
        videoList.setVideoList(videos);
        // 查询总数
        int total = Objects.requireNonNull(stringRedisTemplate.opsForList().size(key)).intValue();
        videoList.setTotal(total);

        // 5.返回vo
        return ResponseResult.successResult(videoList);
    }

    /**
     * 获取用户的作品数量
     *
     * @param userId 用户id
     * @return ResponseResult<Integer> 作品数量
     */
    @Override
    public ResponseResult<Integer> getUserWorks(Long userId) {
        // 1.校验参数
        if (userId == null) {
            throw new NullParamException();
        }

        // 2.获取key
        String userWorkKey = VideoConstant.USER_VIDEO_SET_LIST + userId;

        // 3.查询redis
        Long size = stringRedisTemplate.opsForList().size(userWorkKey);

        // 4.返回数量
        return ResponseResult.successResult(size.intValue());
    }

    /**
     * 评论视频
     * @param videoId 视频id
     * @param parentId 关联的评论
     * @param content 评论内容
     * @return ResponseResult 操作结果
     */
    @Override
    public ResponseResult doComment(Long videoId, Long parentId, String content) {
        // 1.校验参数
        if (videoId == null || content == null) {
            throw new NullParamException();
        }
        if (parentId == null) {
            parentId = 0L;
        }

        // 2.获取当前用户id
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            throw new UserNotLoginException();
        }

        // 3.先向数据库中保存数据
        Comment comment = Comment.builder()
                .userId(userId)
                .content(content)
                .parentId(parentId)
                .status(CommentConstant.STATUS_NORMAL)
                .build();
        try {
            commentMapper.insert(comment);
        }catch (Exception e) {
            log.error("向数据库保存评论信息失败: {}",e.toString());
            throw new DbOperationException("保存评论失败");
        }

        // 4.向redis中保存数据
        // 4.1获取key
        String key = VideoConstant.STRING_COMMENT_KEY + videoId;
        dbOpsService.addIntSafely(key,1);

        // 5.返回操作成功
        return ResponseResult.successResult();
    }

    /**
     * 得到当前评论的子评论
     * @param commentId 当前评论的id
     * @param videoId 视频id
     * @return ResponseResult 评论集合
     */
    @Override
    public ResponseResult getCommentList(Long commentId, Long videoId) {
        // 1.校验参数
        if (commentId == null || videoId == null) {
            throw new NullParamException();
        }

        // 2.直接从数据库中查询
        List<Comment> comments = commentMapper.selectList(Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getParentId, commentId)
                .eq(Comment::getVideoId, videoId)
        );

        // 3.返回结果
        return ResponseResult.successResult(comments);
    }
}
