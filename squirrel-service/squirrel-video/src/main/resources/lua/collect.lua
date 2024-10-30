--keys
--KEYS[1] 视频收藏set的键
--KEYS[2] 视频收藏数量的键
--KEYS[3] 视频作者收藏总数的键

--values
--ARGV[1] 当前用户的id

-- 1.视频收藏
redis.call('sadd',KEYS[1],ARGV[1])

-- 2.增加视频收藏数量
redis.call('incr',KEYS[2])

-- 3.增加视频作者收藏总数
redis.call('incr',KEYS[3])