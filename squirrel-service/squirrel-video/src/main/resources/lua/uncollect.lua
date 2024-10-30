--keys
--KEYS[1] 视频收藏set的键
--KEYS[2] 视频收藏数量的键
--KEYS[3] 视频作者收藏总数的键

--values
--ARGV[1] 当前用户的id

-- 1.删除数据
redis.call('srem',KEYS[1],ARGV[1])

-- 2.减少视频收藏数量
redis.call('decr',KEYS[2])

-- 3.减少视频作者收藏总数
redis.call('decr',KEYS[3])