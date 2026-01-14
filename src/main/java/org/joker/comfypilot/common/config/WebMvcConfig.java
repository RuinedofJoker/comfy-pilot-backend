package org.joker.comfypilot.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册 TraceId 拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TraceIdInterceptor traceIdInterceptor;

    @Autowired
    public WebMvcConfig(TraceIdInterceptor traceIdInterceptor) {
        this.traceIdInterceptor = traceIdInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册 TraceId 拦截器，拦截所有请求
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**")
                .order(1); // 设置为最高优先级
    }
}
