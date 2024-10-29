-- key 定义
-- KEYS[1] 当前用户的关注集合键
-- KEYS[2] 当前用户的关注总数键
-- KEYS[3] 当前用户的互关集合键
-- KEYS[4] 目标对象的关注集合键
-- KEYS[5] 目标对象的互关集合键

-- value 定义
-- ARGV[1] 当前用户id
-- ARGV[2] 目标对象用户id

-- 1.将关注信息存入 redis
if redis.call("sadd", KEYS[1],ARGV[2]) == 0 then
    return redis.error_reply("already follow!")
end

-- 2.将 Redis 中的关注总数加一
redis.call("incr",KEYS[2])

-- 3.检查是否是互关好友，如果是则加入互关列表
if redis.call("sismember",KEYS[4],ARGV[1]) == 1 then
    -- 双方互关
    redis.call("sadd",KEYS[3],ARGV[2])
    redis.call("sadd",KEYS[5],ARGV[1])
end

return 1