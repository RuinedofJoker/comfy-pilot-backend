# 认证模块 - API设计

> 本文档定义认证模块的API接口

## 接口列表

### 1. 用户注册

**接口说明**: 新用户注册账户

**请求**:
- 方法: `POST`
- 路径: `/api/v1/auth/register`
- Content-Type: `application/json`

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "email": "user@example.com"
  }
}
```

### 2. 用户登录

**接口说明**: 用户邮箱密码登录

**请求**:
- 方法: `POST`
- 路径: `/api/v1/auth/login`
- Content-Type: `application/json`

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "username": "张三",
      "avatarUrl": "https://example.com/avatar.jpg"
    }
  }
}
```

### 3. 用户登出

**接口说明**: 用户登出，撤销当前Token

**请求**:
- 方法: `POST`
- 路径: `/api/v1/auth/logout`
- 认证: 需要 Bearer Token

**响应**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 4. 刷新Token

**接口说明**: 使用刷新令牌获取新的访问令牌

**请求**:
- 方法: `POST`
- 路径: `/api/v1/auth/refresh`
- Content-Type: `application/json`

**请求体**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应**:
```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

### 5. 请求密码重置

**接口说明**: 发送密码重置邮件

**请求**:
- 方法: `POST`
- 路径: `/api/v1/auth/forgot-password`
- Content-Type: `application/json`

**请求体**:
```json
{
  "email": "user@example.com"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "重置邮件已发送",
  "data": null
}
```

### 6. 确认密码重置

**接口说明**: 使用重置令牌设置新密码

**请求**:
- 方法: `POST`
- 路径: `/api/v1/auth/reset-password`
- Content-Type: `application/json`

**请求体**:
```json
{
  "token": "uuid-token-string",
  "newPassword": "newpassword123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```
