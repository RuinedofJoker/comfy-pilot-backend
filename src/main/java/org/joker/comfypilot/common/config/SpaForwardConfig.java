package org.joker.comfypilot.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class SpaForwardConfig implements WebMvcConfigurer {

    @Value("${frontend.dist-path}")
    private String frontendPath;

    /**
     * 1. 显式将根路径 / 映射到 index.html
     * 这可以跳过 Spring 对根路径 "." 的错误解析
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = frontendPath.replace("\\", "/");
        if (!location.endsWith("/")) {
            location += "/";
        }
        if (!location.startsWith("file:")) {
            location = "file:" + location;
        }

        registry.addResourceHandler("/**")
                .addResourceLocations(location)
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // 如果请求的是 index.html 本身，直接返回
                        if ("index.html".equals(resourcePath)) {
                            return location.createRelative("index.html");
                        }

                        // 尝试在 dist 目录找对应的物理文件 (js, css, img...)
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // 如果是 API 请求，返回 null 让 Spring 去找 Controller，不要回退到 index.html
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }

                        // 对于 SPA 的其他路由（如 /dashboard），统一返回 index.html
                        return location.createRelative("index.html");
                    }
                });
    }
}