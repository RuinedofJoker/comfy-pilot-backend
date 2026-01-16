# 用户模块和认证模块实现步骤

> 本文档记录了用户模块和认证模块的完整实现过程

## 📋 目录

- [一、需求分析](#一需求分析)
- [二、用户模块实现](#二用户模块实现)
- [三、认证模块实现](#三认证模块实现)
- [四、集成与配置](#四集成与配置)
- [五、待完善功能](#五待完善功能)

---

## 一、需求分析

### 1.1 阅读设计文档

阅读并确认了以下设计文档:

**用户模块文档**:
- `docs/requirements/03-用户模块/数据模型.md` - 用户表设计
- `docs/requirements/03-用户模块/API设计.md` - 用户API接口
- `docs/requirements/03-用户模块/模块设计.md` - 用户业务逻辑

**认证模块文档**:
- `docs/requirements/04-认证模块/数据模型.md` - Redis数据结构设计
- `docs/requirements/04-认证模块/API设计.md` - 认证API接口
- `docs/requirements/04-认证模块/模块设计.md` - 认证业务逻辑

### 1.2 核心需求

**用户模块**:
- 获取当前用户信息
- 更新用户信息(用户名、头像)

**认证模块**:
- 用户注册(邮箱+密码)
- 用户登录(返回JWT Token)
- 用户登出(撤销Token)
- Token刷新
- 密码重置(发送邮件、重置密码)

**技术要求**:
- Token和Session存储在Redis
- 使用ThreadLocal传递用户上下文
- 使用拦截器自动认证请求
- 使用BCrypt加密密码
- 遵循DDD四层架构

---

## 二、用户模块实现

### 2.1 领域层 (Domain)

#### 2.1.1 创建用户状态枚举

**文件**: `src/main/java/org/joker/comfypilot/user/domain/enums/UserStatus.java`

```java
public enum UserStatus {
    ACTIVE("ACTIVE", "活跃"),
    INACTIVE("INACTIVE", "未激活"),
    LOCKED("LOCKED", "锁定"),
    DELETED("DELETED", "已删除");
}
```

**说明**: 定义用户的四种状态,使用MyBatis-Plus的@EnumValue注解存储到数据库

#### 2.1.2 创建用户领域实体

**文件**: `src/main/java/org/joker/comfypilot/user/domain/entity/User.java`

**核心字段**:
- id - 用户ID
- userCode - 用户编码(唯一)
- email - 邮箱(唯一)
- username - 用户名
- passwordHash - 密码哈希
- avatarUrl - 头像URL
- status - 用户状态
- lastLoginTime - 最后登录时间
- lastLoginIp - 最后登录IP

**核心方法**:
- `updateUsername()` - 更新用户名(带验证)
- `updateAvatarUrl()` - 更新头像
- `updateLastLogin()` - 更新登录信息
- `canLogin()` - 检查是否可登录
- `lock()` - 锁定用户
- `activate()` - 激活用户

#### 2.1.3 创建用户仓储接口

**文件**: `src/main/java/org/joker/comfypilot/user/domain/repository/UserRepository.java`

**核心方法**:
- `findById()` - 根据ID查询
- `findByEmail()` - 根据邮箱查询
- `findByUserCode()` - 根据用户编码查询
- `existsByEmail()` - 检查邮箱是否存在
- `save()` - 保存用户
- `deleteById()` - 删除用户

### 2.2 基础设施层 (Infrastructure)

#### 2.2.1 创建用户持久化对象

**文件**: `src/main/java/org/joker/comfypilot/user/infrastructure/persistence/po/UserPO.java`

**说明**: 继承BasePO,使用@TableName注解映射到"user"表

#### 2.2.2 创建MyBatis Mapper

**文件**: `src/main/java/org/joker/comfypilot/user/infrastructure/persistence/mapper/UserMapper.java`

```java
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
```

**说明**: 继承MyBatis-Plus的BaseMapper,自动获得CRUD方法

#### 2.2.3 创建实体转换器

**文件**: `src/main/java/org/joker/comfypilot/user/infrastructure/persistence/converter/UserConverter.java`

**核心方法**:
- `toDomain()` - PO转领域实体
- `toPO()` - 领域实体转PO

#### 2.2.4 创建仓储实现类

**文件**: `src/main/java/org/joker/comfypilot/user/infrastructure/persistence/repository/UserRepositoryImpl.java`

**说明**: 实现UserRepository接口,使用UserMapper和UserConverter完成数据访问

### 2.3 应用层 (Application)

#### 2.3.1 创建DTO类

**文件1**: `src/main/java/org/joker/comfypilot/user/application/dto/UserDTO.java`
- 用户信息响应DTO
- 包含id、email、username、avatarUrl、status、lastLoginTime、createTime

**文件2**: `src/main/java/org/joker/comfypilot/user/application/dto/UpdateUserRequest.java`
- 更新用户请求DTO
- 包含username、avatarUrl
- 使用@Size注解验证用户名长度

#### 2.3.2 创建服务接口

**文件**: `src/main/java/org/joker/comfypilot/user/application/service/UserService.java`

**核心方法**:
- `getCurrentUser()` - 获取当前用户信息
- `updateUser()` - 更新用户信息

#### 2.3.3 创建服务实现类

**文件**: `src/main/java/org/joker/comfypilot/user/application/service/impl/UserServiceImpl.java`

**实现逻辑**:
1. 获取用户信息: 从数据库查询并转换为DTO
2. 更新用户信息: 验证、更新、保存、返回DTO

### 2.4 接口层 (Interfaces)

#### 2.4.1 创建REST控制器

**文件**: `src/main/java/org/joker/comfypilot/user/interfaces/controller/UserController.java`

**API接口**:
- `GET /api/v1/users/me` - 获取当前用户信息
- `PUT /api/v1/users/me` - 更新用户信息

**说明**: 使用UserContextHolder.getCurrentUserId()从ThreadLocal获取当前用户ID

---

## 三、认证模块实现

### 3.1 领域层 (Domain)

#### 3.1.1 创建Token类型枚举

**文件**: `src/main/java/org/joker/comfypilot/auth/domain/enums/TokenType.java`

```java
public enum TokenType {
    ACCESS("ACCESS", "访问令牌"),
    REFRESH("REFRESH", "刷新令牌");
}
```

### 3.2 基础设施层 (Infrastructure)

#### 3.2.1 创建Redis数据模型

**文件1**: `src/main/java/org/joker/comfypilot/auth/infrastructure/redis/model/UserTokenRedis.java`
- 用户Token模型
- 字段: userId, token, tokenType, expiresAt, isRevoked, revokedAt, createTime

**文件2**: `src/main/java/org/joker/comfypilot/auth/infrastructure/redis/model/PasswordResetTokenRedis.java`
- 密码重置令牌模型
- 字段: userId, token, expiresAt, isUsed, usedAt, createTime

**文件3**: `src/main/java/org/joker/comfypilot/auth/infrastructure/redis/model/UserSessionRedis.java`
- 用户会话模型
- 字段: userId, email, username, roles, permissions, lastAccessTime

#### 3.2.2 创建JWT工具类

**文件**: `src/main/java/org/joker/comfypilot/auth/infrastructure/util/JwtUtil.java`

**核心方法**:
- `generateAccessToken()` - 生成访问令牌(24小时)
- `generateRefreshToken()` - 生成刷新令牌(7天)
- `getUserIdFromToken()` - 从Token获取用户ID
- `validateToken()` - 验证Token有效性
- `getExpirationFromToken()` - 获取过期时间

**配置项**:
- `jwt.secret` - JWT密钥
- `jwt.access-token-expiration` - 访问令牌过期时间(默认86400000ms)
- `jwt.refresh-token-expiration` - 刷新令牌过期时间(默认604800000ms)

#### 3.2.3 创建Token Redis仓储

**文件**: `src/main/java/org/joker/comfypilot/auth/infrastructure/redis/repository/TokenRedisRepository.java`

**Redis Key设计**:
- `auth:access_token:{token}` - 访问令牌
- `auth:refresh_token:{token}` - 刷新令牌
- `auth:user_tokens:{userId}` - 用户Token列表(Set)

**核心方法**:
- `saveAccessToken()` - 保存访问令牌(TTL 24小时)
- `saveRefreshToken()` - 保存刷新令牌(TTL 7天)
- `getAccessToken()` - 获取访问令牌
- `getRefreshToken()` - 获取刷新令牌
- `revokeToken()` - 撤销令牌

#### 3.2.4 创建Session Redis仓储

**文件**: `src/main/java/org/joker/comfypilot/auth/infrastructure/redis/repository/SessionRedisRepository.java`

**Redis Key设计**:
- `auth:session:{userId}` - 用户会话(Hash)

**核心方法**:
- `saveSession()` - 保存用户会话(TTL 24小时)
- `getSession()` - 获取用户会话
- `deleteSession()` - 删除用户会话
- `refreshSession()` - 刷新会话过期时间

#### 3.2.5 创建用户上下文ThreadLocal

**文件**: `src/main/java/org/joker/comfypilot/auth/infrastructure/context/UserContextHolder.java`

**核心方法**:
- `setUserSession()` - 设置当前用户会话
- `getUserSession()` - 获取当前用户会话
- `getCurrentUserId()` - 获取当前用户ID
- `clear()` - 清除上下文

**说明**: 使用ThreadLocal存储用户会话,避免在方法间传递用户信息

#### 3.2.6 创建认证拦截器

**文件**: `src/main/java/org/joker/comfypilot/auth/infrastructure/interceptor/AuthInterceptor.java`

**拦截逻辑**:
1. 从请求头获取Authorization: Bearer {token}
2. 验证JWT Token有效性
3. 从Redis查询Token信息,检查是否撤销
4. 从Redis获取用户会话
5. 设置到ThreadLocal
6. 刷新会话过期时间
7. 请求结束后清除ThreadLocal

### 3.3 应用层 (Application)

#### 3.3.1 创建DTO类

**请求DTO**:
- `RegisterRequest.java` - 注册请求(email, password)
- `LoginRequest.java` - 登录请求(email, password)
- `RefreshTokenRequest.java` - 刷新Token请求(refreshToken)
- `ForgotPasswordRequest.java` - 忘记密码请求(email)
- `ResetPasswordRequest.java` - 重置密码请求(token, newPassword)

**响应DTO**:
- `RegisterResponse.java` - 注册响应(userId, email)
- `LoginResponse.java` - 登录响应(accessToken, refreshToken, expiresIn, user)
- `RefreshTokenResponse.java` - 刷新Token响应(accessToken, expiresIn)

**验证规则**:
- 邮箱: @Email注解
- 密码: 最小8位,包含字母和数字(@Pattern注解)

#### 3.3.2 创建服务接口

**文件**: `src/main/java/org/joker/comfypilot/auth/application/service/AuthService.java`

**核心方法**:
- `register()` - 用户注册
- `login()` - 用户登录
- `logout()` - 用户登出
- `refreshToken()` - 刷新Token
- `forgotPassword()` - 请求密码重置
- `resetPassword()` - 确认密码重置

#### 3.3.3 创建服务实现类

**文件**: `src/main/java/org/joker/comfypilot/auth/application/service/impl/AuthServiceImpl.java`

**1. 用户注册逻辑**:
```
1. 验证邮箱唯一性
2. 生成用户编码(USER_随机16位)
3. 使用BCrypt加密密码
4. 创建用户实体(状态为ACTIVE)
5. 保存到数据库
6. 返回userId和email
```

**2. 用户登录逻辑**:
```
1. 根据邮箱查询用户
2. 验证密码(BCrypt.matches)
3. 检查用户状态(ACTIVE才能登录)
4. 生成accessToken和refreshToken
5. 保存Token到Redis
6. 创建用户会话到Redis
7. 更新最后登录时间和IP
8. 返回Token和用户信息
```

**3. 用户登出逻辑**:
```
1. 撤销Token(设置isRevoked=true)
2. 删除用户会话
3. 记录日志
```

**4. 刷新Token逻辑**:
```
1. 验证refreshToken有效性
2. 从Redis检查是否撤销
3. 生成新的accessToken
4. 保存到Redis
5. 返回新Token
```

**5. 忘记密码逻辑** (待完善):
```
1. 验证邮箱是否存在
2. 生成UUID重置令牌
3. 保存到Redis(TTL 15分钟)
4. 发送重置邮件(TODO)
```

**6. 重置密码逻辑** (待完善):
```
1. 从Redis验证重置令牌
2. 检查是否过期或已使用
3. 使用BCrypt加密新密码
4. 更新用户密码
5. 标记令牌为已使用
6. 撤销所有现有Token
```

### 3.4 接口层 (Interfaces)

#### 3.4.1 创建REST控制器

**文件**: `src/main/java/org/joker/comfypilot/auth/interfaces/controller/AuthController.java`

**API接口**:
- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录
- `POST /api/v1/auth/logout` - 用户登出
- `POST /api/v1/auth/refresh` - 刷新Token
- `POST /api/v1/auth/forgot-password` - 请求密码重置
- `POST /api/v1/auth/reset-password` - 确认密码重置

---

## 四、集成与配置

### 4.1 密码编码器配置

**文件**: `src/main/java/org/joker/comfypilot/auth/config/PasswordEncoderConfig.java`

```java
@Configuration
public class PasswordEncoderConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.2 拦截器配置

**文件**: `src/main/java/org/joker/comfypilot/auth/config/AuthInterceptorConfig.java`

**拦截规则**:
- 拦截所有 `/api/**` 路径
- 排除认证相关接口:
  - `/api/v1/auth/register`
  - `/api/v1/auth/login`
  - `/api/v1/auth/refresh`
  - `/api/v1/auth/forgot-password`
  - `/api/v1/auth/reset-password`

### 4.3 数据库表创建

**用户表SQL** (已在文档中定义):
```sql
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    user_code VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_user_code UNIQUE (user_code),
    CONSTRAINT uk_user_email UNIQUE (email)
);
```

---

## 五、待完善功能

### 5.1 密码重置功能

**当前状态**: 基础框架已实现,但缺少以下部分:

1. **PasswordResetToken Redis仓储**
   - 需要创建 `PasswordResetTokenRedisRepository`
   - 实现保存、查询、标记已使用等方法

2. **邮件发送功能**
   - 需要集成通知模块
   - 发送密码重置邮件

3. **完善resetPassword方法**
   - 从Redis获取重置令牌信息
   - 验证令牌有效性
   - 更新用户密码
   - 撤销所有现有Token

### 5.2 获取真实IP

**当前状态**: 登录时使用硬编码的"127.0.0.1"

**改进方案**:
```java
// 从HttpServletRequest获取真实IP
private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty()) {
        ip = request.getHeader("X-Real-IP");
    }
    if (ip == null || ip.isEmpty()) {
        ip = request.getRemoteAddr();
    }
    return ip;
}
```

### 5.3 权限模块集成

**当前状态**: 用户会话中的roles和permissions使用空列表

**待实现**:
1. 实现权限模块
2. 在登录时从权限模块获取用户角色和权限
3. 更新 `createUserSession()` 方法

---

## 六、技术亮点

### 6.1 架构设计

- **DDD四层架构**: 严格遵循领域驱动设计,职责清晰
- **依赖倒置**: 领域层定义接口,基础设施层实现
- **单一职责**: 每个类只负责一个功能

### 6.2 安全性

- **BCrypt加密**: 使用BCrypt对密码进行不可逆加密
- **JWT Token**: 使用JWT生成Token,支持过期时间
- **Token黑名单**: 使用Redis存储Token,支持撤销
- **密码强度验证**: 最小8位,必须包含字母和数字

### 6.3 性能优化

- **Redis缓存**: Token和Session全部存储在Redis
- **TTL自动过期**: 利用Redis的TTL特性自动清理过期数据
- **ThreadLocal**: 避免在方法间传递用户信息

### 6.4 代码质量

- **参数校验**: 使用JSR-303注解进行参数验证
- **异常处理**: 统一的异常处理机制
- **日志记录**: 关键操作都有日志记录
- **Swagger文档**: 完整的API文档注解

---

## 七、文件清单

### 7.1 用户模块文件 (13个)

**领域层**:
1. `user/domain/enums/UserStatus.java`
2. `user/domain/entity/User.java`
3. `user/domain/repository/UserRepository.java`

**基础设施层**:
4. `user/infrastructure/persistence/po/UserPO.java`
5. `user/infrastructure/persistence/mapper/UserMapper.java`
6. `user/infrastructure/persistence/repository/UserRepositoryImpl.java`
7. `user/infrastructure/persistence/converter/UserConverter.java`

**应用层**:
8. `user/application/dto/UserDTO.java`
9. `user/application/dto/UpdateUserRequest.java`
10. `user/application/service/UserService.java`
11. `user/application/service/impl/UserServiceImpl.java`

**接口层**:
12. `user/interfaces/controller/UserController.java`

### 7.2 认证模块文件 (22个)

**领域层**:
1. `auth/domain/enums/TokenType.java`

**基础设施层**:
2. `auth/infrastructure/redis/model/UserTokenRedis.java`
3. `auth/infrastructure/redis/model/PasswordResetTokenRedis.java`
4. `auth/infrastructure/redis/model/UserSessionRedis.java`
5. `auth/infrastructure/redis/repository/TokenRedisRepository.java`
6. `auth/infrastructure/redis/repository/SessionRedisRepository.java`
7. `auth/infrastructure/util/JwtUtil.java`
8. `auth/infrastructure/context/UserContextHolder.java`
9. `auth/infrastructure/interceptor/AuthInterceptor.java`

**应用层**:
10. `auth/application/dto/RegisterRequest.java`
11. `auth/application/dto/RegisterResponse.java`
12. `auth/application/dto/LoginRequest.java`
13. `auth/application/dto/LoginResponse.java`
14. `auth/application/dto/RefreshTokenRequest.java`
15. `auth/application/dto/RefreshTokenResponse.java`
16. `auth/application/dto/ForgotPasswordRequest.java`
17. `auth/application/dto/ResetPasswordRequest.java`
18. `auth/application/service/AuthService.java`
19. `auth/application/service/impl/AuthServiceImpl.java`

**接口层**:
20. `auth/interfaces/controller/AuthController.java`

**配置层**:
21. `auth/config/PasswordEncoderConfig.java`
22. `auth/config/AuthInterceptorConfig.java`

**总计**: 35个Java文件

---

## 八、测试建议

### 8.1 单元测试

**用户模块**:
- UserServiceImpl测试
- UserRepositoryImpl测试
- UserConverter测试

**认证模块**:
- AuthServiceImpl测试
- JwtUtil测试
- TokenRedisRepository测试
- SessionRedisRepository测试

### 8.2 集成测试

**用户模块**:
- GET /api/v1/users/me
- PUT /api/v1/users/me

**认证模块**:
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/logout
- POST /api/v1/auth/refresh

### 8.3 测试场景

1. **注册流程**:
   - 正常注册
   - 邮箱已存在
   - 密码强度不足

2. **登录流程**:
   - 正常登录
   - 邮箱不存在
   - 密码错误
   - 用户状态异常

3. **Token刷新**:
   - 正常刷新
   - refreshToken无效
   - refreshToken已撤销

4. **登出流程**:
   - 正常登出
   - Token已撤销

5. **用户信息**:
   - 获取用户信息
   - 更新用户信息
   - 未登录访问

---

## 九、部署配置

### 9.1 application.yml配置

```yaml
# JWT配置
jwt:
  secret: comfy-pilot-secret-key-for-jwt-token-generation-2024
  access-token-expiration: 86400000  # 24小时
  refresh-token-expiration: 604800000  # 7天

# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 3000ms

# 数据库配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/comfy_pilot
    username: postgres
    password: your_password
```

### 9.2 依赖配置

**pom.xml**:
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Spring Security (仅用于BCrypt) -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

---

## 十、总结

本次实现完成了用户模块和认证模块的核心功能,严格遵循DDD架构和项目规范。代码结构清晰,职责明确,易于维护和扩展。

**完成度**:
- ✅ 用户模块: 100%
- ✅ 认证模块: 90% (密码重置功能待完善)

**下一步工作**:
1. 完善密码重置功能
2. 实现权限模块
3. 编写单元测试和集成测试
4. 完善API文档
5. 性能测试和优化
