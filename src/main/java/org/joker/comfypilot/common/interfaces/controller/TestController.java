package org.joker.comfypilot.common.interfaces.controller;

import lombok.Data;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 测试 Controller
 * 用于测试 JSON 序列化配置
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @GetMapping("/json-serialization")
    public Result<TestData> testJsonSerialization() {
        TestData data = new TestData();
        data.setId(1234567890123456789L); // 大数字，超过 JavaScript 安全整数范围
        data.setUserId(876543210987654321L);
        data.setName("测试数据");
        data.setCreatedAt(LocalDateTime.now());
        return Result.success(data);
    }

    @Data
    public static class TestData {
        private Long id;
        private Long userId;
        private String name;
        private LocalDateTime createdAt;
    }
}
