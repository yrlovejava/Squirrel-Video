package com.squirrel.filter;

import com.squirrel.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 登录过滤器
 */
@Order(0)
@Component
@Slf4j
public class LoginFilter implements GlobalFilter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 过滤器
     * @param exchange 请求上下文
     * @param chain 过滤器链
     * @return 过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        RequestPath path = exchange.getRequest().getPath();
        // 如果是登录请求 或者 注册请求，直接放行
        if(path.toString().contains("/login") || path.toString().contains("/register")) {
            return chain.filter(exchange);
        }
        // 如果是其他请求，判断是否已经登录
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        String userId = stringRedisTemplate.opsForValue().get(UserConstant.REDIS_LOGIN_TOKEN + token);
        if(userId == null) {
            // 未登录
            // 日志记录
            log.info("拦截到未登录的请求:{}",path);
            // 返回错误状态码
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        // 如果已经登录
        // 刷新 token 有效期
        stringRedisTemplate.opsForValue().set(UserConstant.REDIS_LOGIN_TOKEN + token,userId,UserConstant.LOGIN_USER_TTL, TimeUnit.SECONDS);
        // 放行
        return chain.filter(exchange);
    }
}
