package org.joker.comfypilot.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 */
@Configuration
@MapperScan("org.joker.comfypilot.*.infrastructure.persistence.mapper")
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * 包含分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        // 设置请求的页面大于最大页后操作，true调回到首页，false继续请求  默认false
        paginationInnerInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(1000L);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}
