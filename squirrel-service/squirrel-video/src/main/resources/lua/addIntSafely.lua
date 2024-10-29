-- 获取当前值，如果不存在则默认为0
local exNum = tonumber(redis.call("get", KEYS[1])) or 0

-- 将新的值存入Redis
local now = exNum + tonumber(ARGV[1])
redis.call("set", KEYS[1], tostring(now))

return now