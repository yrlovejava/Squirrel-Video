--keys
--KEYS[1] 视频点赞set的键
--KEYS[2] 视频点赞数量的键
--KEYS[3] 视频作者点赞总数的键
--KEYS[4] 当前用户点赞的视频的集合的键

--values
--ARGV[1] 当前用户的id
--ARGV[2] 视频的id

-- 1.视频点赞
redis.call('sadd',KEYS[1],ARGV[1])

-- 2.增加视频点赞数量
redis.call('incr',KEYS[2])

-- 3.增加视频作者点赞总数
redis.call('incr',KEYS[3])

-- 4.在用户点赞视频的集合的键
redis.call('sadd',KEYS[4],ARGV[2])