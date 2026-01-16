# 权限模块实现步骤

> 本文档记录了权限模块的完整实现过程以及与认证模块的集成

## 📋 目录

- [一、Step1 遗留问题](#一step1-遗留问题)
- [二、Step2 目标](#二step2-目标)
- [三、权限模块实现](#三权限模块实现)
- [四、认证模块集成](#四认证模块集成)
- [五、系统初始化](#五系统初始化)
- [六、数据库迁移](#六数据库迁移)
- [七、完善遗留问题](#七完善遗留问题)
- [八、测试验证](#八测试验证)

---

## 一、Step1 遗留问题

### 1.1 待完善功能

从 Step1 继承的未完成任务：

1. **权限模块集成** ⭐ (Step2 主要任务)
   - 当前状态：用户会话中的 roles 和 permissions 使用空列表
   - 需要实现：完整的权限模块，包括角色管理、权限定义、用户角色分配

2. **获取真实 IP** ⭐ (Step2 完成)
   - 当前状态：登录时使用硬编码的 "127.0.0.1"
   - 需要实现：从 HttpServletRequest 获取真实客户端 IP

3. **密码重置功能** ⏸️ (部分完成)
   - 当前状态：基础框架已实现，缺少 PasswordResetTokenRedisRepository 和邮件发送
   - Step2 计划：实现 Redis 仓储，邮件发送留待通知模块

---

## 二、Step2 目标

### 2.1 核心目标

**实现权限模块的核心功能，并与认证模块深度集成**

### 2.2 功能范围

**权限模块核心功能**：
- 角色管理（Role）
- 权限定义管理（Permission）
- 用户角色关联（UserRole）
- 角色权限关联（RolePermission）
- 权限查询服务
- 权限缓存机制

**认证模块集成**：
- 登录时加载用户角色和权限
- 注册时自动分配默认角色
- Session 中存储真实的权限信息

**系统初始化**：
- 应用启动时创建系统内置角色
- 初始化基础权限定义

### 2.3 技术要求

- 遵循 DDD 四层架构
- 使用 Redis 缓存用户权限信息（TTL 24小时）
- 权限格式：`资源:操作`（如 `workflow:create`）
- 系统内置角色不可删除
- 新用户默认分配 `USER` 角色

---

## 三、权限模块实现

### 3.1 领域层 (Domain)

#### 3.1.1 创建角色实体

**文件**: `src/main/java/org/joker/comfypilot/permission/domain/entity/Role.java`

**核心字段**:
- id - 角色ID
- roleCode - 角色编码（唯一，如 ADMIN、USER）
- roleName - 角色名称
- description - 角色描述
- isSystem - 是否系统内置角色

**核心方法**:
- `updateRoleName()` - 更新角色名称
- `updateDescription()` - 更新角色描述
- `canDelete()` - 检查是否可删除（系统角色不可删除）

#### 3.1.2 创建权限实体

**文件**: `src/main/java/org/joker/comfypilot/permission/domain/entity/Permission.java`

**核心字段**:
- id - 权限ID
- permissionCode - 权限编码（唯一，如 workflow:create）
- permissionName - 权限名称
- resourceType - 资源类型（如 workflow、user）
- description - 权限描述

#### 3.1.3 创建用户角色关联实体

**文件**: `src/main/java/org/joker/comfypilot/permission/domain/entity/UserRole.java`

**核心字段**:
- id - 关联ID
- userId - 用户ID
- roleId - 角色ID

#### 3.1.4 创建角色权限关联实体

**文件**: `src/main/java/org/joker/comfypilot/permission/domain/entity/RolePermission.java`

**核心字段**:
- id - 关联ID
- roleId - 角色ID
- permissionId - 权限ID

#### 3.1.5 创建仓储接口

**文件1**: `src/main/java/org/joker/comfypilot/permission/domain/repository/RoleRepository.java`

**核心方法**:
- `findById()` - 根据ID查询角色
- `findByRoleCode()` - 根据角色编码查询
- `existsByRoleCode()` - 检查角色编码是否存在
- `save()` - 保存角色
- `deleteById()` - 删除角色

**文件2**: `src/main/java/org/joker/comfypilot/permission/domain/repository/PermissionRepository.java`

**核心方法**:
- `findById()` - 根据ID查询权限
- `findByPermissionCode()` - 根据权限编码查询
- `findByIds()` - 批量查询权限
- `save()` - 保存权限

**文件3**: `src/main/java/org/joker/comfypilot/permission/domain/repository/UserRoleRepository.java`

**核心方法**:
- `findByUserId()` - 查询用户的所有角色关联
- `findRolesByUserId()` - 查询用户的所有角色实体
- `save()` - 保存用户角色关联
- `deleteByUserIdAndRoleId()` - 删除用户角色关联

**文件4**: `src/main/java/org/joker/comfypilot/permission/domain/repository/RolePermissionRepository.java`

**核心方法**:
- `findByRoleId()` - 查询角色的所有权限关联
- `findPermissionsByRoleId()` - 查询角色的所有权限实体
- `findPermissionsByRoleIds()` - 批量查询多个角色的权限
- `save()` - 保存角色权限关联

---

### 3.2 基础设施层 (Infrastructure)

#### 3.2.1 创建持久化对象

**文件1**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/po/RolePO.java`

**说明**: 继承 BasePO，使用 @TableName("role") 注解映射到 role 表

**文件2**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/po/PermissionPO.java`

**说明**: 继承 BasePO，使用 @TableName("permission") 注解映射到 permission 表

**文件3**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/po/UserRolePO.java`

**说明**: 使用 @TableName("user_role") 注解映射到 user_role 表

**文件4**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/po/RolePermissionPO.java`

**说明**: 使用 @TableName("role_permission") 注解映射到 role_permission 表

#### 3.2.2 创建 MyBatis Mapper

**文件1**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/mapper/RoleMapper.java`

```java
@Mapper
public interface RoleMapper extends BaseMapper<RolePO> {
}
```

**文件2**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/mapper/PermissionMapper.java`

```java
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionPO> {
}
```

**文件3**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/mapper/UserRoleMapper.java`

```java
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {
    // 自定义查询：根据用户ID查询角色列表
    @Select("SELECT r.* FROM role r " +
            "INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = false")
    List<RolePO> findRolesByUserId(@Param("userId") Long userId);
}
```

**文件4**: `src/main/java/org/joker/comfypilot/permission/infrastructure/persistence/mapper/RolePermissionMapper.java`

```java
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionPO> {
    // 自定义查询：根据角色ID列表查询权限列表
    @Select("<script>" +
            "SELECT DISTINCT p.* FROM permission p " +
            "INNER JOIN role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            " AND p.is_deleted = false" +
            "</script>")
    List<PermissionPO> findPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
```

---

## 四、认证模块集成

(待实现)

---

## 五、系统初始化

(待实现)

---

## 六、数据库迁移

(待实现)

---

## 七、完善遗留问题

(待实现)

---

## 八、测试验证

(待实现)
