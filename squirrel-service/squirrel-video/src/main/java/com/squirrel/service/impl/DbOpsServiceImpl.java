package com.squirrel.service.impl;

import com.squirrel.constant.VideoConstant;
import com.squirrel.exception.LuaExecuteException;
import com.squirrel.mapper.VideoMapper;
import com.squirrel.model.video.pojos.Video;
import com.squirrel.model.video.pojos.VideoLike;
import com.squirrel.service.DbOpsService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据库操作的实现类，包括 redis 和 mongoDB
 */
@Slf4j
@Service
public class DbOpsServiceImpl implements DbOpsService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private VideoMapper videoMapper;

    /**
     * 增加值的 Lua 脚本
     */
    private static final DefaultRedisScript<Long> ADD_INT_SAFELY_SCRIPT;

    static {
        ADD_INT_SAFELY_SCRIPT = new DefaultRedisScript<>();
        // 设置脚本位置
        ADD_INT_SAFELY_SCRIPT.setLocation(new ClassPathResource("/lua/addIntSafely.lua"));
        // 设置返回类型
        ADD_INT_SAFELY_SCRIPT.setResultType(Long.class);
    }

    /**
     * 安全的向 redis 的 set 中添加值(使用 lua 脚本)
     *
     * @param key 视频id
     * @param num 点赞数
     */
    @Override
    public void addIntSafely(String key, int num) {
        log.info("点赞数增加: {}", key);
        try {
            stringRedisTemplate.execute(ADD_INT_SAFELY_SCRIPT, Collections.singletonList(key), String.valueOf(num));
        } catch (Exception e) {
            log.error("lua脚本执行失败: {}", e.toString());
            throw new LuaExecuteException();
        }
    }

    /**
     * 将数据异步插入 mongoDB 中
     *
     * @param userId  用户id
     * @param videoId 视频id
     * @param type    操作类型(1 点赞 2收藏 3评论)
     * @param ops     添加到对应字段的参数
     */
    @Override
    @Async(value = "mongoThreadPoolExecutor") // 设置自定义的线程池
    public void insertIntoMongoDB(Long userId, Long videoId, int type, Object ops) {
        // 1.查询当用户id和视频id所在的字段
        // 1.1封装查询条件
        Criteria criteria = Criteria
                .where("userId").is(userId)
                .and("videoId").is(videoId);
        Query query = Query.query(criteria);
        // 1.2查找点赞实体
        VideoLike videoLike = mongoTemplate.findOne(query, VideoLike.class);

        // 2.检查是否存在
        if (videoLike == null) {
            // 不存在，添加 userId 和 videoId
            videoLike = new VideoLike();
            videoLike.setUserId(userId);
            videoLike.setVideoId(videoId);
        }

        // 3.根据类型对相应的字段进行更新操作
        switch (type) {
            //点赞
            case 1:
                videoLike.setIsLike((Integer) ops);
                break;
            //收藏
            case 2:
                videoLike.setIsCollect((Integer) ops);
                break;
            //评论
            case 3:
                videoLike.setCommentList((List<String>) ops);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        mongoTemplate.save(videoLike);
    }

    /**
     * 要是发现redis中like字段过期，则从数据库中查询数据返回，并同时把此视频所有字段刷新到redis
     * @param videoId 视频id
     * @return 点赞数
     */
    @Override
    public Long getSumFromDB(Long videoId) {
        // 从数据库中查询当前 video 的数据
        Video video = videoMapper.selectById(videoId);
        // 刷新到redis，同时刷新likes,collects,comments字段
        stringRedisTemplate.opsForValue().set(VideoConstant.STRING_LIKE_KEY + videoId,video.getLikes().toString());
        stringRedisTemplate.opsForValue().set(VideoConstant.STRING_COLLECT_KEY + video,video.getCollects().toString());
        stringRedisTemplate.opsForValue().set(VideoConstant.STRING_COMMENT_KEY + video,video.getComments().toString());
        return video.getLikes();
    }

    /**
     * 把 redis 中 kv 类似的数据定时刷新到 mysql 中
     * 每 12 小时执行一次
     */
    @PostConstruct
    @Scheduled(cron = "0 */ 720 * * * ?")
    public void refresh() {
//        Set<String> likeKeys = stringRedisTemplate.keys(VideoConstant.STRING_LIKE_KEY + '*');
//        Set<String> collectKeys = stringRedisTemplate.keys(VideoConstant.STRING_COLLECT_KEY + '*');
//        Set<String> commentKeys = stringRedisTemplate.keys(VideoConstant.STRING_COMMENT_KEY + '*');
        Set<String> likeKeys = scanKeys(VideoConstant.STRING_LIKE_KEY + '*');
        Set<String> collectKeys = scanKeys(VideoConstant.STRING_COLLECT_KEY + '*');
        Set<String> commentKeys = scanKeys(VideoConstant.STRING_COMMENT_KEY + '*');

        if(!likeKeys.isEmpty()){
            // 更新点赞数
            for (String likeKey : likeKeys) {
                // 获取 videoId
                String sub = likeKey.substring(VideoConstant.STRING_LIKE_KEY.length());
                long videoId = Long.parseLong(sub);
                // 创建新的video对象
                Video video = new Video();
                video.setId(videoId);
                // 获取点赞数
                String likes = stringRedisTemplate.opsForValue().get(likeKey);
                long likesNum = Long.parseLong(likes == null ? "0" : likes);
                video.setLikes(likesNum);
                // 刷新到数据库
                videoMapper.updateById(video);
            }
        }

        if (!collectKeys.isEmpty()){
            // 更新收藏数
            for (String collectKey : collectKeys) {
                // 获取 videoId
                String sub = collectKey.substring(VideoConstant.STRING_COLLECT_KEY.length());
                long videoId = Long.parseLong(sub);
                // 创建新的video对象
                Video video = new Video();
                video.setId(videoId);
                // 获取收藏数
                String collects = stringRedisTemplate.opsForValue().get(collectKey);
                long collectsNum = Long.parseLong(collects == null ? "0" : collects);
                video.setCollects(collectsNum);
                // 更新数据库
                videoMapper.updateById(video);
            }
        }

        if (!commentKeys.isEmpty()){
            // 更新评论数
            for (String commentKey : commentKeys) {
                // 得到videoId
                // 获取 videoId
                String sub = commentKey.substring(VideoConstant.STRING_COLLECT_KEY.length());
                long videoId = Long.parseLong(sub);
                // 创建新的video对象
                Video video = new Video();
                video.setId(videoId);
                // 获取收藏数
                String comments = stringRedisTemplate.opsForValue().get(commentKey);
                long commentNum = Long.parseLong(comments == null ? "0" : comments);
                video.setCollects(commentNum);
                // 更新数据库
                videoMapper.updateById(video);
            }
        }
    }

    /**
     * scan查找所有key
     *
     * @param pattern 匹配模式
     * @return key的set集合
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();

        // 设置 SCAN 命令
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern) // 匹配模式
                .count(100) // 每次扫描 100 条
                .build();

        // 使用 RedisTemplate 执行 SCAN
        try (Cursor<byte[]> scanCursor = stringRedisTemplate.execute((RedisCallback<Cursor<byte[]>>) conn -> conn.scan(options), true)) {
            // 执行扫描并获取结果
            if (scanCursor != null) {
                while (scanCursor.hasNext()) {
                    // 将字节数组转换为字符串
                    String key = new String(scanCursor.next(), StandardCharsets.UTF_8);
                    keys.add(key);
                }
            }
        }

        return keys;
    }
}
