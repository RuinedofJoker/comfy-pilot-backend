package org.joker.comfypilot.test.interfaces.controller;

import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.test.application.dto.TestUserDTO;
import org.joker.comfypilot.test.application.service.TestUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用户 Controller
 * 用于测试 MyBatis-Plus 和数据库连接功能
 */
@RestController
@RequestMapping("/api/v1/test/users")
public class TestUserController {

    @Autowired
    private TestUserService testUserService;

    /**
     * 查询所有用户
     * GET /api/v1/test/users
     */
    @GetMapping
    public Result<List<TestUserDTO>> listAll() {
        List<TestUserDTO> users = testUserService.listAll();
        return Result.success(users);
    }

    /**
     * 根据ID查询用户
     * GET /api/v1/test/users/{id}
     */
    @GetMapping("/{id}")
    public Result<TestUserDTO> getById(@PathVariable Long id) {
        TestUserDTO user = testUserService.getById(id);
        return Result.success(user);
    }

    /**
     * 创建用户
     * POST /api/v1/test/users
     */
    @PostMapping
    public Result<TestUserDTO> create(@RequestBody TestUserDTO dto) {
        TestUserDTO created = testUserService.create(dto);
        return Result.success("创建成功", created);
    }
}
