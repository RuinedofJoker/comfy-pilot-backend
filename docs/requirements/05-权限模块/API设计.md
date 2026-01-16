# 权限模块 - API设计

> 本文档定义权限模块的API接口

## 接口列表

### 1. 获取当前用户角色

**接口说明**: 获取当前登录用户的角色列表

**请求**:
- 方法: `GET`
- 路径: `/api/v1/permissions/my-roles`
- 认证: 需要 Bearer Token

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "roleCode": "ADMIN",
      "roleName": "管理员"
    },
    {
      "roleCode": "USER",
      "roleName": "普通用户"
    }
  ]
}
```

### 2. 获取当前用户权限

**接口说明**: 获取当前登录用户的权限列表

**请求**:
- 方法: `GET`
- 路径: `/api/v1/permissions/my-permissions`
- 认证: 需要 Bearer Token

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    "workflow:create",
    "workflow:read",
    "workflow:update",
    "workflow:delete"
  ]
}
```
