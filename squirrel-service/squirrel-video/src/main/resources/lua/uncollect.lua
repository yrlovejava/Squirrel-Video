--keys
--KEYS[1] 视频收藏set的键
--KEYS[2] 视频收藏数量的键
--KEYS[3] 视频作者收藏总数的键
--KEYS[4] 当前用户点赞的视频的集合的键

--values
--ARGV[1] 当前用户的id
--ARGV[2] 视频的id

-- 1.删除数据
if (redis.call('srem',KEYS[1],ARGV[1]) == 0) then
    redis.error_reply("already uncollect!")
end

-- 2.减少视频收藏数量
redis.call('decr',KEYS[2])

-- 3.减少视频作者收藏总数
redis.call('decr',KEYS[3])

-- 4.从当前用户的收藏视频集合中删除
redis.call('srem',KEYS[4],ARGV[2])