-- key 定义
-- KEYS[1] 当前用户的关注集合键
-- KEYS[2] 当前用户的关注总数键
-- KEYS[3] 当前用户的互关集合键
-- KEYS[4] 目标对象的关注集合键
-- KEYS[5] 目标对象的互关集合键

-- value 定义
-- ARGV[1] 当前用户id
-- ARGV[2] 目标对象用户id

-- 1.查询是否关注
if redis.call("sismember", KEYS[1],ARGV[2]) == 0 then
    return redis.error_reply("please follow!")
end

-- 2.将关注总数减一
redis.call("decr", KEYS[2])

-- 3.检查是否是互关好友，如果是则从互关列表中删除
if redis.call("sismember",KEYS[4],ARGV[1]) == 1 then
    redis.call("srem", KEYS[3], ARGV[2])
    redis.call("srem", KEYS[5], ARGV[1])
end

-- 4.将目标用户从当前用户的关注列表中删除
redis.call("srem",KEYS[1],ARGV[2])

return 1