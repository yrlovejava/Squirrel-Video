package com.squirrel.interceptor;

import com.squirrel.utils.ThreadLocalUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * feign 请求全局添加Token
 */
@Configuration
public class MyFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("userId", ThreadLocalUtil.getUserId().toString());
    }
}
