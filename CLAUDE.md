# CLAUDE.md

Always respond in Chinese-simplified

## Cursor Rules Specifications Summary

The project maintains comprehensive coding standards and patterns in `.cursor/rules/` directory.In every new conversation, you should read all the rules and then follow them.

@.cursor/rules/**/*.md

All code must strictly adhere to these patterns and standards for consistency and maintainability.

每个新的会话需要操纵或阅读当前`后端代码仓库`前先阅读当前后端代码仓库根目录下的`.cursor/rules/`下的所有规则文件
阅读完后端代码仓库根目录下的`.cursor/rules/`下的所有规则文件
阅读完后端代码仓库根目录下的`.cursor/rules/`下的所有规则文件
阅读完后端代码仓库根目录下的`.cursor/rules/`下的所有规则文件
阅读完后端代码仓库根目录下的`docs/requirements`需求目录下的`README.md`、00-项目概述.md`、`99-技术规范.md`
阅读完后端代码仓库根目录下的`docs/requirements`需求目录下的`README.md`、00-项目概述.md`、`99-技术规范.md`
阅读完后端代码仓库根目录下的`docs/requirements`需求目录下的`README.md`、00-项目概述.md`、`99-技术规范.md`
并根据上面的规则和需求索引文件来结合用户的问题查询`docs/requirements`需求目录下对应模块的模块设计、api设计、数据库设计文件

当前阶段为模块实现阶段：
据UI设计图里的页面和需求文档设计指定模块的文档和代码
当前阶段每一步实现都会在当前后端代码仓库根目录下的steps目录下新建一个step[x].md(x为当前该目录下最大的值+1，如之前最大的是step1.md，当前步记录文件为step2.md)
用户会告诉你当前是在第多少步(如当前我们在step1)，当前步结束时用户会告诉你我需要新建一步，这时你需要将当前步的内容记录到当前步文件里，然后新建一个新的步文件开始新的步
新的步创建时需要继承上一步没有做完的事，如果需要新步的大纲也需要在根据老步创建新步时指定到新步文件里
每一步都只需要看上一步做了什么，不需要关注更之前的步

你不需要启动项目，因为当前环境的jdk和我IDE环境里的jdk不是同一个，你直接启动会报错

---

## 新模块开发规范

### 一、模块结构规范

#### 1.1 DDD四层架构
```
org.joker.comfypilot.{module}/
├── interfaces/          # 接口层（用户接口层）
│   ├── controller/      # REST API 控制器
│   ├── dto/            # 数据传输对象
│   └── converter/      # DTO转换器（可选）
├── application/         # 应用层
│   ├── service/        # 应用服务
│   ├── dto/            # 应用层DTO（如果需要）
│   └── converter/      # Entity到DTO转换器
├── domain/             # 领域层
│   ├── entity/         # 领域实体
│   ├── repository/     # 仓储接口
│   └── enums/          # 领域枚举（可选）
└── infrastructure/     # 基础设施层
    ├── persistence/    # 持久化实现
    │   ├── po/         # 持久化对象
    │   ├── mapper/     # MyBatis Mapper接口
    │   ├── converter/  # PO转换器
    │   └── repository/ # 仓储实现
    └── config/         # 模块配置（可选）
```

#### 1.2 支撑服务模块（三层架构）
对于不对外暴露REST API的支撑服务模块（如notification），可以省略interfaces层：
```
org.joker.comfypilot.{module}/
├── application/         # 应用层
├── domain/             # 领域层
└── infrastructure/     # 基础设施层
```

---

### 二、核心类规范

#### 2.1 Entity（领域实体）

**必须遵循：**
- ✅ 继承 `BaseEntity<Long>`
- ✅ 添加 `serialVersionUID = 1L`
- ✅ 使用 `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- ✅ 包含业务逻辑方法（领域行为）

**示例：**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResource extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String fileName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 领域行为方法
    public void incrementDownloadCount() {
        if (this.downloadCount == null) {
            this.downloadCount = 0;
        }
        this.downloadCount++;
    }
}
```

#### 2.2 PO（持久化对象）

**必须遵循：**
- ✅ 继承 `BasePO`
- ✅ 添加 `serialVersionUID = 1L`
- ✅ 使用 `@Data`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@EqualsAndHashCode(callSuper = true)`
- ✅ 使用 `@TableName` 指定表名
- ✅ 不包含业务逻辑，仅用于持久化

**示例：**
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("file_resource")
public class FileResourcePO extends BasePO {

    private static final long serialVersionUID = 1L;

    private String fileName;
    private String storedName;
    private Long fileSize;
}
```

#### 2.3 DTO（数据传输对象）

**必须遵循：**
- ✅ 继承 `BaseDTO`（已包含id, createTime, updateTime）
- ✅ 添加 `serialVersionUID = 1L`
- ✅ 使用 `@Data`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@EqualsAndHashCode(callSuper = true)`
- ✅ 添加 `@Schema` Swagger注解（类级别和字段级别）
- ✅ 不重复定义BaseDTO中已有的字段（id, createTime, updateTime）

**BaseDTO包含的字段：**
```java
@Data
@SuperBuilder
public abstract class BaseDTO implements Serializable {
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**示例：**
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件资源信息")
public class FileResourceDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;
}
```

---

### 三、Converter转换器规范

#### 3.1 Infrastructure层Converter（PO ↔ Entity）

**必须遵循：**
- ✅ 使用 `@Mapper(componentModel = "spring")` 注解
- ✅ 定义为接口，由MapStruct自动生成实现
- ✅ 提供 `toDomain()` 和 `toPO()` 方法
- ✅ 如需特殊转换，使用 `@Mapping` 和 `@Named` 自定义方法

**示例：**
```java
@Mapper(componentModel = "spring")
public interface FileResourceConverter {

    /**
     * PO转领域实体
     */
    FileResource toDomain(FileResourcePO po);

    /**
     * 领域实体转PO
     */
    FileResourcePO toPO(FileResource domain);
}
```

**枚举转换示例：**
```java
@Mapper(componentModel = "spring")
public interface EmailLogConverter {

    @Mapping(target = "sendStatus", source = "sendStatus", qualifiedByName = "stringToEnum")
    EmailLog toDomain(EmailLogPO po);

    @Mapping(target = "sendStatus", source = "sendStatus", qualifiedByName = "enumToString")
    EmailLogPO toPO(EmailLog domain);

    @Named("stringToEnum")
    default EmailSendStatus stringToEnum(String value) {
        return value != null ? EmailSendStatus.valueOf(value) : null;
    }

    @Named("enumToString")
    default String enumToString(EmailSendStatus status) {
        return status != null ? status.name() : null;
    }
}
```

#### 3.2 Application层Converter（Entity ↔ DTO）

**必须遵循：**
- ✅ 使用 `@Mapper(componentModel = "spring")` 注解
- ✅ 定义为接口，由MapStruct自动生成实现
- ✅ 提供 `toDTO()` 方法（Entity → DTO）
- ✅ Entity和DTO的字段名已统一为 `createTime/updateTime`，无需额外映射

**示例：**
```java
@Mapper(componentModel = "spring")
public interface FileResourceDTOConverter {

    /**
     * Entity转DTO
     * 字段名已统一，MapStruct自动映射
     */
    FileResourceDTO toDTO(FileResource entity);
}
```

---

### 四、Controller规范

**必须遵循：**
- ✅ 使用 `@RestController` 和 `@RequestMapping`
- ✅ 使用 `@RequiredArgsConstructor` 进行依赖注入
- ✅ 添加 `@Tag` 类级别Swagger注解
- ✅ 每个方法添加 `@Operation` Swagger注解
- ✅ 参数添加 `@Parameter` Swagger注解
- ✅ 统一返回 `Result<T>` 类型
- ✅ 注入并使用 DTOConverter 进行转换

**示例：**
```java
@Tag(name = "文件资源", description = "文件上传、下载、管理相关接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileResourceController {

    private final FileUploadService fileUploadService;
    private final FileResourceDTOConverter dtoConverter;

    @Operation(summary = "上传单个文件", description = "上传单个文件到服务器")
    @PostMapping("/upload")
    public Result<FileResourceDTO> uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file) {

        FileResource fileResource = fileUploadService.uploadFile(file);
        return Result.success(dtoConverter.toDTO(fileResource));
    }
}
```

---

### 五、开发流程检查清单

#### 5.1 创建新模块前的检查
- [ ] 确认模块名称和职责边界
- [ ] 确认是否需要对外暴露REST API（决定是否需要interfaces层）
- [ ] 查看 `docs/requirements` 中的模块设计文档
- [ ] 参考已有模块（如user模块）的结构

#### 5.2 开发过程中的检查
- [ ] Entity继承BaseEntity并添加serialVersionUID
- [ ] PO继承BasePO并添加serialVersionUID，使用@SuperBuilder
- [ ] DTO继承BaseDTO并添加serialVersionUID，使用@SuperBuilder，不重复定义基类字段
- [ ] 所有Converter使用MapStruct接口（@Mapper(componentModel = "spring")）
- [ ] Controller注入DTOConverter，不使用手动转换方法
- [ ] 所有DTO和Controller添加完整的Swagger注解
- [ ] Entity和DTO的时间字段统一使用createTime/updateTime

#### 5.3 完成后的验证
- [ ] 编译通过，无类型错误
- [ ] 所有类都有正确的包结构
- [ ] Swagger文档生成正确
- [ ] 遵循DDD分层调用规则（Controller → Service → Repository）

---

### 六、常见错误与修复

#### 6.1 缺少serialVersionUID
**错误：** PO或Entity类没有serialVersionUID
**修复：** 添加 `private static final long serialVersionUID = 1L;`

#### 6.2 DTO重复定义基类字段
**错误：** DTO中定义了id、createTime等BaseDTO已有字段
**修复：** 删除重复字段，使用继承的字段

#### 6.3 使用手动转换而非MapStruct
**错误：** Converter使用@Component和手动builder
**修复：** 改为@Mapper(componentModel = "spring")接口

#### 6.4 Controller中手动转换DTO
**错误：** Controller中使用私有方法toDTO()手动转换
**修复：** 注入DTOConverter，使用dtoConverter.toDTO()

#### 6.5 缺少Swagger注解
**错误：** DTO或Controller缺少@Schema或@Operation注解
**修复：** 参考user模块添加完整的Swagger注解

#### 6.6 DTO使用@Builder而非@SuperBuilder
**错误：** DTO继承BaseDTO但使用@Builder注解
**修复：** 改为@SuperBuilder以支持父类字段的构建

---

### 七、参考模块

#### 7.1 标准参考模块
- **user模块**：完整的四层架构示例，包含所有标准实现
- **resource模块**：文件上传下载业务示例
- **notification模块**：三层架构示例（无interfaces层）

#### 7.2 关键参考文件
```
user/
├── domain/entity/User.java              # Entity标准写法
├── infrastructure/persistence/
│   ├── po/UserPO.java                   # PO标准写法
│   └── converter/UserConverter.java     # Infrastructure层Converter
├── application/dto/UserDTO.java         # DTO标准写法
└── interfaces/controller/UserController.java  # Controller标准写法
```

---

### 八、SOLID原则应用

#### 8.1 单一职责原则（SRP）
- Entity：仅包含领域逻辑和业务规则
- PO：仅用于数据库持久化
- DTO：仅用于数据传输
- Converter：仅负责对象转换
- Controller：仅处理HTTP请求响应
- Service：仅处理业务流程编排

#### 8.2 开闭原则（OCP）
- 使用接口定义Converter，易于扩展
- 使用MapStruct自动生成实现，无需修改现有代码

#### 8.3 依赖倒置原则（DIP）
- Controller依赖Service接口而非实现
- Service依赖Repository接口而非实现
- 使用Spring依赖注入管理依赖关系

---

### 九、快速开发步骤

#### 9.1 创建新模块的标准流程

**步骤1：定义领域实体（Domain Layer）**
```bash
# 创建目录
mkdir -p src/main/java/org/joker/comfypilot/{module}/domain/entity
mkdir -p src/main/java/org/joker/comfypilot/{module}/domain/repository

# 创建Entity和Repository接口
```

**步骤2：创建持久化层（Infrastructure Layer）**
```bash
# 创建目录
mkdir -p src/main/java/org/joker/comfypilot/{module}/infrastructure/persistence/po
mkdir -p src/main/java/org/joker/comfypilot/{module}/infrastructure/persistence/mapper
mkdir -p src/main/java/org/joker/comfypilot/{module}/infrastructure/persistence/converter
mkdir -p src/main/java/org/joker/comfypilot/{module}/infrastructure/persistence/repository

# 创建PO、Mapper、Converter、RepositoryImpl
```

**步骤3：创建应用层（Application Layer）**
```bash
# 创建目录
mkdir -p src/main/java/org/joker/comfypilot/{module}/application/service
mkdir -p src/main/java/org/joker/comfypilot/{module}/application/converter

# 创建Service和DTOConverter
```

**步骤4：创建接口层（Interfaces Layer）**
```bash
# 创建目录
mkdir -p src/main/java/org/joker/comfypilot/{module}/interfaces/controller
mkdir -p src/main/java/org/joker/comfypilot/{module}/interfaces/dto

# 创建Controller和DTO
```

#### 9.2 开发顺序建议
1. Entity（领域模型）→ 2. Repository接口 → 3. PO → 4. Mapper → 5. Converter(PO↔Entity) → 6. RepositoryImpl → 7. Service → 8. DTO → 9. Converter(Entity↔DTO) → 10. Controller

---

### 十、总结

遵循本规范可以确保：
- ✅ 代码结构清晰，符合DDD架构
- ✅ 对象转换统一使用MapStruct，减少重复代码
- ✅ API文档完整，便于前后端协作
- ✅ 符合SOLID原则，代码易于维护和扩展
- ✅ 与现有模块保持一致的代码风格