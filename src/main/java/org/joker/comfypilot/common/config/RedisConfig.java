package org.joker.comfypilot.common.config;

import com.alibaba.fastjson2.support.spring6.data.redis.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 配置 Redis 序列化器，使用 Fastjson2
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * 使用 Fastjson2 作为 Value 序列化器
     * 注意：GenericFastJsonRedisSerializer 会自动处理类型信息，无需全局 AutoType 配置
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String 序列化器（用于 Key）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Fastjson2 序列化器（用于 Value）
        // GenericFastJsonRedisSerializer 内部已配置必要的 Feature，无需额外全局配置
        GenericFastJsonRedisSerializer fastJsonSerializer = new GenericFastJsonRedisSerializer();

        // Key 使用 String 序列化
        template.setKeySerializer(stringSerializer);
        // Value 使用 Fastjson2 序列化
        template.setValueSerializer(fastJsonSerializer);

        // Hash Key 使用 String 序列化
        template.setHashKeySerializer(stringSerializer);
        // Hash Value 使用 Fastjson2 序列化
        template.setHashValueSerializer(fastJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
