--keys
--KEYS[1] video信息key
--KEYS[2] 最新video的idkey
--KYES[3] 用户发布的video的list的key

--values
--ARGV[1] video json字符串
--ARGV[2] videoId

-- 1.设置videoKey -> video Json字符串
redis.call('set',KEYS[1],ARGV[1])

-- 2.设置最新的id
redis.call('set',KEYS[2],ARGV[2])

-- 3.在用户发布视频的list中添加videoId
redis.call('lpush',KEYS[3],ARGV[2])