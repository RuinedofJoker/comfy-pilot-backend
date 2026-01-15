# 用户模块 API 文档

## 基础信息
- 基础路径: `/api/v1`
- 认证方式: JWT Token (除登录注册接口外)
- 请求头: `Authorization: Bearer {token}`

---

## 1. 用户认证

### 1.1 用户注册
**接口**: `POST /auth/register`

**请求体**:
```json
{
  "username": "string",      // 用户名，3-50字符，唯一
  "password": "string",      // 密码，6-20字符
  "email": "string",         // 邮箱，可选
  "displayName": "string"    // 显示名称，可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "displayName": "测试用户",
    "email": "test@example.com",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 用户登录
**接口**: `POST /auth/login`

**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "testuser",
      "displayName": "测试用户",
      "role": "USER"
    }
  }
}
```

---

### 1.3 用户登出
**接口**: `POST /auth/logout`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.4 刷新Token
**接口**: `POST /auth/refresh`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

---

## 2. 用户管理

### 2.1 获取当前用户信息
**接口**: `GET /users/me`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "displayName": "测试用户",
    "email": "test@example.com",
    "avatarUrl": "https://example.com/avatar.jpg",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2026-01-15T10:00:00",
    "updateTime": "2026-01-15T10:00:00"
  }
}
```

---

### 2.2 更新用户信息
**接口**: `PUT /users/me`

**请求体**:
```json
{
  "displayName": "string",
  "email": "string",
  "avatarUrl": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "displayName": "新名称",
    "email": "newemail@example.com",
    "avatarUrl": "https://example.com/new-avatar.jpg",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

---

### 2.3 修改密码
**接口**: `POST /users/me/change-password`

**请求体**:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 2.4 获取用户列表 (管理员)
**接口**: `GET /users`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10
- `role`: 角色筛选，可选
- `status`: 状态筛选，可选

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": 1,
        "username": "testuser",
        "displayName": "测试用户",
        "email": "test@example.com",
        "role": "USER",
        "status": "ACTIVE",
        "createTime": "2026-01-15T10:00:00"
      }
    ]
  }
}
```

---

### 2.5 获取指定用户信息 (管理员)
**接口**: `GET /users/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "displayName": "测试用户",
    "email": "test@example.com",
    "avatarUrl": "https://example.com/avatar.jpg",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2026-01-15T10:00:00",
    "updateTime": "2026-01-15T10:00:00"
  }
}
```

---

### 2.6 更新用户信息 (管理员)
**接口**: `PUT /users/{id}`

**请求体**:
```json
{
  "displayName": "string",
  "email": "string",
  "role": "USER|ADMIN",
  "status": "ACTIVE|DISABLED"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "displayName": "新名称",
    "email": "newemail@example.com",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

---

### 2.7 删除用户 (管理员)
**接口**: `DELETE /users/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未认证或Token无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突(如用户名已存在) |
| 500 | 服务器内部错误 |

---

## 枚举值说明

### UserRole (用户角色)
- `ADMIN`: 管理员
- `USER`: 普通用户

### UserStatus (用户状态)
- `ACTIVE`: 激活
- `DISABLED`: 禁用
