# 用户模块 - API设计

> 本文档定义用户模块的API接口

## 接口列表

### 1. 获取当前用户信息

**接口说明**: 获取当前登录用户的详细信息

**请求**:
- 方法: `GET`
- 路径: `/api/v1/users/me`
- 认证: 需要 Bearer Token

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "张三",
    "avatarUrl": "https://example.com/avatar.jpg",
    "status": "ACTIVE",
    "lastLoginTime": "2024-01-15T10:30:00Z",
    "createTime": "2024-01-01T08:00:00Z"
  }
}
```

### 2. 更新用户信息

**接口说明**: 更新当前用户的基本信息（用户名、头像等）

**请求**:
- 方法: `PUT`
- 路径: `/api/v1/users/me`
- 认证: 需要 Bearer Token
- Content-Type: `application/json`

**请求体**:
```json
{
  "username": "李四",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "李四",
    "avatarUrl": "https://example.com/new-avatar.jpg",
    "status": "ACTIVE",
    "lastLoginTime": "2024-01-15T10:30:00Z",
    "createTime": "2024-01-01T08:00:00Z"
  }
}
```
