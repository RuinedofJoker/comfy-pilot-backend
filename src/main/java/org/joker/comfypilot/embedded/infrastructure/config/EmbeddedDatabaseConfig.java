package org.joker.comfypilot.embedded.infrastructure.config;

import cn.hutool.core.exceptions.ExceptionUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.service.AgentRegistry;
import org.joker.comfypilot.common.util.EmbeddedDatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(AgentRegistry.class)
@Slf4j
public class EmbeddedDatabaseConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${embedded.database}")
    private boolean enableEmbeddedDatabase;

    @PostConstruct
    public void init() {
        if (enableEmbeddedDatabase) {
            log.info("开始初始化内嵌H2数据库Schema。。。");

            // 初始化各模块表
            try {
                initUserTables();
                initPermissionTables();
                initResourceAndNotificationTables();
                initComfyUIServerTables();
                initWorkflowTables();
                initModelTables();
                initAgentTables();
                initSessionTables();
            } catch (Exception e) {
                log.error("初始化内嵌H2数据库失败", e);
                if (ExceptionUtil.stacktraceToString(e).contains("Failed to obtain JDBC Connection")) {
                    EmbeddedDatabaseUtil.INITIALIZED_EMBEDDED_DATABASE = false;
                }
            }

            log.info("内嵌H2数据库Schema初始化完成");
        } else {
            log.info("当前未开启内嵌H2数据库");
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName.toUpperCase());
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查表{}是否存在时出错", tableName, e);
            return false;
        }
    }

    /**
     * 初始化用户模块表
     */
    private void initUserTables() {
        if (tableExists("user")) {
            log.info("用户表已存在，跳过创建");
            return;
        }

        log.info("开始创建用户表...");
        String sql = """
                CREATE TABLE IF NOT EXISTS "user" (
                    id BIGINT PRIMARY KEY,
                    user_code VARCHAR(50) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    username VARCHAR(100) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    avatar_url VARCHAR(500),
                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    last_login_time TIMESTAMP,
                    last_login_ip VARCHAR(50),
                    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    create_by BIGINT,
                    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_by BIGINT,
                    is_deleted BIGINT NOT NULL DEFAULT 0,
                    CONSTRAINT uk_user_code UNIQUE (user_code),
                    CONSTRAINT uk_user_email UNIQUE (email)
                )
                """;

        jdbcTemplate.execute(sql);

        // 创建索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_email ON \"user\"(email)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_status ON \"user\"(status)");

        log.info("用户表创建完成");
    }

    /**
     * 初始化权限模块表
     */
    private void initPermissionTables() {
        // 1. 创建角色表
        if (!tableExists("role")) {
            log.info("开始创建角色表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS role (
                        id BIGINT PRIMARY KEY,
                        role_code VARCHAR(50) NOT NULL,
                        role_name VARCHAR(100) NOT NULL,
                        description VARCHAR(500),
                        is_system BOOLEAN NOT NULL DEFAULT FALSE,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_by BIGINT,
                        is_deleted BIGINT NOT NULL DEFAULT 0,
                        CONSTRAINT uk_role_code UNIQUE (role_code)
                    )
                    """;
            jdbcTemplate.execute(sql);
            log.info("角色表创建完成");
        } else {
            log.info("角色表已存在，跳过创建");
        }

        // 2. 创建权限表
        if (!tableExists("permission")) {
            log.info("开始创建权限表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS permission (
                        id BIGINT PRIMARY KEY,
                        permission_code VARCHAR(100) NOT NULL,
                        permission_name VARCHAR(100) NOT NULL,
                        resource_type VARCHAR(50) NOT NULL,
                        description VARCHAR(500),
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_by BIGINT,
                        is_deleted BIGINT NOT NULL DEFAULT 0,
                        CONSTRAINT uk_permission_code UNIQUE (permission_code)
                    )
                    """;
            jdbcTemplate.execute(sql);
            log.info("权限表创建完成");
        } else {
            log.info("权限表已存在，跳过创建");
        }

        // 3. 创建角色权限关联表
        if (!tableExists("role_permission")) {
            log.info("开始创建角色权限关联表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS role_permission (
                        id BIGINT PRIMARY KEY,
                        role_id BIGINT NOT NULL,
                        permission_id BIGINT NOT NULL,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
                    )
                    """;
            jdbcTemplate.execute(sql);
            log.info("角色权限关联表创建完成");
        } else {
            log.info("角色权限关联表已存在，跳过创建");
        }

        // 4. 创建用户角色关联表
        if (!tableExists("user_role")) {
            log.info("开始创建用户角色关联表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS user_role (
                        id BIGINT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        role_id BIGINT NOT NULL,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
                    )
                    """;
            jdbcTemplate.execute(sql);
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON user_role(user_id)");
            log.info("用户角色关联表创建完成");
        } else {
            log.info("用户角色关联表已存在，跳过创建");
        }
    }

    /**
     * 初始化资源和通知模块表
     */
    private void initResourceAndNotificationTables() {
        // 1. 创建文件资源表
        if (!tableExists("file_resource")) {
            log.info("开始创建文件资源表...");
            String sql = """
                    CREATE TABLE file_resource (
                        id BIGINT PRIMARY KEY,
                        file_name VARCHAR(255) NOT NULL,
                        stored_name VARCHAR(255) NOT NULL,
                        file_path VARCHAR(500) NOT NULL,
                        file_size BIGINT NOT NULL,
                        file_type VARCHAR(100),
                        file_extension VARCHAR(50),
                        upload_user_id BIGINT NOT NULL,
                        business_type VARCHAR(50),
                        business_id BIGINT,
                        download_count INT DEFAULT 0,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT NOT NULL,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_by BIGINT NOT NULL,
                        is_deleted BIGINT DEFAULT 0
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_file_resource_upload_user_id ON file_resource(upload_user_id)");
            jdbcTemplate.execute("CREATE INDEX idx_file_resource_business ON file_resource(business_type, business_id)");
            jdbcTemplate.execute("CREATE UNIQUE INDEX idx_file_resource_stored_name ON file_resource(stored_name, is_deleted)");
            jdbcTemplate.execute("CREATE INDEX idx_file_resource_create_time ON file_resource(create_time)");

            log.info("文件资源表创建完成");
        } else {
            log.info("文件资源表已存在，跳过创建");
        }

        // 2. 创建邮件发送日志表
        if (!tableExists("email_log")) {
            log.info("开始创建邮件发送日志表...");
            String sql = """
                    CREATE TABLE email_log (
                        id BIGINT PRIMARY KEY,
                        recipient VARCHAR(255) NOT NULL,
                        subject VARCHAR(500) NOT NULL,
                        content CLOB NOT NULL,
                        send_status VARCHAR(20) NOT NULL,
                        error_message CLOB,
                        send_time TIMESTAMP,
                        retry_count INT DEFAULT 0,
                        business_type VARCHAR(50),
                        business_id VARCHAR(100),
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT NOT NULL,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_by BIGINT NOT NULL,
                        is_deleted BIGINT DEFAULT 0
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_email_log_recipient ON email_log(recipient)");
            jdbcTemplate.execute("CREATE INDEX idx_email_log_send_status ON email_log(send_status)");
            jdbcTemplate.execute("CREATE INDEX idx_email_log_business ON email_log(business_type, business_id)");
            jdbcTemplate.execute("CREATE INDEX idx_email_log_create_time ON email_log(create_time)");

            log.info("邮件发送日志表创建完成");
        } else {
            log.info("邮件发送日志表已存在，跳过创建");
        }
    }

    /**
     * 初始化ComfyUI服务模块表
     */
    private void initComfyUIServerTables() {
        if (tableExists("comfyui_server")) {
            log.info("ComfyUI服务表已存在，跳过创建");
            return;
        }

        log.info("开始创建ComfyUI服务表...");
        String sql = """
                CREATE TABLE IF NOT EXISTS comfyui_server (
                    id BIGINT PRIMARY KEY,
                    server_key VARCHAR(100) NOT NULL,
                    server_name VARCHAR(100) NOT NULL,
                    description VARCHAR(500),
                    base_url VARCHAR(255) NOT NULL,
                    auth_mode VARCHAR(20),
                    api_key VARCHAR(255),
                    timeout_seconds INT NOT NULL DEFAULT 30,
                    max_retries INT NOT NULL DEFAULT 3,
                    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                    last_health_check_time TIMESTAMP,
                    health_status VARCHAR(20),
                    advanced_features_enabled BOOLEAN DEFAULT FALSE,
                    advanced_features CLOB,
                    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    create_by BIGINT NOT NULL,
                    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_by BIGINT NOT NULL,
                    is_deleted BIGINT NOT NULL DEFAULT 0,
                    CONSTRAINT uk_server_key UNIQUE (server_key)
                )
                """;
        jdbcTemplate.execute(sql);

        // 创建索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_comfyui_server_is_enabled ON comfyui_server(is_enabled)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_comfyui_server_health_status ON comfyui_server(health_status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_comfyui_server_advanced_features_enabled ON comfyui_server(advanced_features_enabled)");

        log.info("ComfyUI服务表创建完成");
    }

    /**
     * 初始化工作流模块表
     */
    private void initWorkflowTables() {
        // 1. 创建workflow表
        if (!tableExists("workflow")) {
            log.info("开始创建工作流表...");
            String sql = """
                    CREATE TABLE workflow (
                        id BIGINT PRIMARY KEY,
                        workflow_name VARCHAR(100) NOT NULL,
                        description VARCHAR(500),
                        comfyui_server_id BIGINT NOT NULL,
                        comfyui_server_key VARCHAR(100) NOT NULL,
                        active_content CLOB,
                        active_content_hash VARCHAR(64),
                        thumbnail_url VARCHAR(500),
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT NOT NULL,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_by BIGINT NOT NULL,
                        is_deleted BIGINT NOT NULL DEFAULT 0
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_workflow_server_id ON workflow(comfyui_server_id)");
            jdbcTemplate.execute("CREATE INDEX idx_workflow_server_key ON workflow(comfyui_server_key)");
            jdbcTemplate.execute("CREATE INDEX idx_workflow_create_by ON workflow(create_by)");

            log.info("工作流表创建完成");
        } else {
            log.info("工作流表已存在，跳过创建");
        }

        // 2. 创建workflow_version表
        if (!tableExists("workflow_version")) {
            log.info("开始创建工作流版本表...");
            String sql = """
                    CREATE TABLE workflow_version (
                        id BIGINT PRIMARY KEY,
                        workflow_id BIGINT NOT NULL,
                        version_code VARCHAR(255) NOT NULL,
                        content CLOB NOT NULL,
                        content_hash VARCHAR(64) NOT NULL,
                        change_summary VARCHAR(500),
                        session_id BIGINT,
                        message_id BIGINT,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT NOT NULL,
                        is_deleted BIGINT NOT NULL DEFAULT 0
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE UNIQUE INDEX uk_workflow_version ON workflow_version(workflow_id, version_code)");
            jdbcTemplate.execute("CREATE INDEX idx_version_workflow_id ON workflow_version(workflow_id)");
            jdbcTemplate.execute("CREATE INDEX idx_version_content_hash ON workflow_version(content_hash)");
            jdbcTemplate.execute("CREATE INDEX idx_version_session_id ON workflow_version(session_id)");
            jdbcTemplate.execute("CREATE INDEX idx_version_message_id ON workflow_version(message_id)");
            jdbcTemplate.execute("CREATE INDEX idx_version_create_time ON workflow_version(create_time)");

            log.info("工作流版本表创建完成");
        } else {
            log.info("工作流版本表已存在，跳过创建");
        }
    }

    /**
     * 初始化AI模型模块表
     */
    private void initModelTables() {
        // 1. 创建model_provider表
        if (!tableExists("model_provider")) {
            log.info("开始创建模型提供商表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS model_provider (
                        id BIGINT PRIMARY KEY,
                        provider_name VARCHAR(100) NOT NULL,
                        provider_type VARCHAR(50) NOT NULL,
                        api_base_url VARCHAR(500),
                        api_key VARCHAR(500),
                        description CLOB,
                        is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP,
                        create_by BIGINT,
                        update_by BIGINT,
                        is_deleted BIGINT NOT NULL DEFAULT 0
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_provider_type ON model_provider(provider_type)");
            jdbcTemplate.execute("CREATE INDEX idx_provider_enabled ON model_provider(is_enabled)");

            log.info("模型提供商表创建完成");
        } else {
            log.info("模型提供商表已存在，跳过创建");
        }

        // 2. 创建ai_model表
        if (!tableExists("ai_model")) {
            log.info("开始创建AI模型表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS ai_model (
                        id BIGINT PRIMARY KEY,
                        model_name VARCHAR(100) NOT NULL,
                        model_identifier VARCHAR(100) NOT NULL UNIQUE,
                        access_type VARCHAR(50) NOT NULL,
                        model_type VARCHAR(50) NOT NULL,
                        model_calling_type VARCHAR(50),
                        api_base_url VARCHAR(500),
                        api_key CLOB,
                        provider_id BIGINT,
                        provider_type VARCHAR(50),
                        model_config CLOB,
                        description CLOB,
                        is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP,
                        create_by BIGINT,
                        update_by BIGINT,
                        is_deleted BIGINT NOT NULL DEFAULT 0
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE UNIQUE INDEX uk_model_identifier ON ai_model(model_identifier)");
            jdbcTemplate.execute("CREATE INDEX idx_model_access_type ON ai_model(access_type)");
            jdbcTemplate.execute("CREATE INDEX idx_model_type ON ai_model(model_type)");
            jdbcTemplate.execute("CREATE INDEX idx_model_calling_type ON ai_model(model_calling_type)");
            jdbcTemplate.execute("CREATE INDEX idx_model_provider_id ON ai_model(provider_id)");
            jdbcTemplate.execute("CREATE INDEX idx_model_provider_type ON ai_model(provider_type)");
            jdbcTemplate.execute("CREATE INDEX idx_model_enabled ON ai_model(is_enabled)");

            log.info("AI模型表创建完成");
        } else {
            log.info("AI模型表已存在，跳过创建");
        }
    }

    /**
     * 初始化Agent模块表
     */
    private void initAgentTables() {
        // 1. 创建agent_config表
        if (!tableExists("agent_config")) {
            log.info("开始创建Agent配置表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS agent_config (
                        id BIGINT PRIMARY KEY,
                        agent_code VARCHAR(50) NOT NULL,
                        agent_name VARCHAR(100) NOT NULL,
                        description CLOB,
                        version VARCHAR(20) NOT NULL,
                        agent_scope_config CLOB,
                        agent_config_definitions CLOB,
                        config CLOB,
                        status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
                        is_deleted BIGINT NOT NULL DEFAULT 0,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT,
                        update_by BIGINT
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_agent_config_status ON agent_config(status)");
            jdbcTemplate.execute("CREATE INDEX idx_agent_config_is_deleted ON agent_config(is_deleted)");

            log.info("Agent配置表创建完成");
        } else {
            log.info("Agent配置表已存在，跳过创建");
        }

        // 2. 创建agent_execution_log表
        if (!tableExists("agent_execution_log")) {
            log.info("开始创建Agent执行日志表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS agent_execution_log (
                        id BIGINT PRIMARY KEY,
                        agent_id BIGINT NOT NULL,
                        session_id BIGINT NOT NULL,
                        input CLOB,
                        output CLOB,
                        status VARCHAR(20) NOT NULL,
                        error_message CLOB,
                        execution_time_ms BIGINT,
                        is_deleted BIGINT NOT NULL DEFAULT 0,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT,
                        update_by BIGINT
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_agent_execution_log_agent_id ON agent_execution_log(agent_id)");
            jdbcTemplate.execute("CREATE INDEX idx_agent_execution_log_session_id ON agent_execution_log(session_id)");
            jdbcTemplate.execute("CREATE INDEX idx_agent_execution_log_status ON agent_execution_log(status)");
            jdbcTemplate.execute("CREATE INDEX idx_agent_execution_log_create_time ON agent_execution_log(create_time)");
            jdbcTemplate.execute("CREATE INDEX idx_agent_execution_log_is_deleted ON agent_execution_log(is_deleted)");

            log.info("Agent执行日志表创建完成");
        } else {
            log.info("Agent执行日志表已存在，跳过创建");
        }
    }

    /**
     * 初始化会话模块表
     */
    private void initSessionTables() {
        // 1. 创建chat_session表
        if (!tableExists("chat_session")) {
            log.info("开始创建聊天会话表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS chat_session (
                        id BIGINT PRIMARY KEY,
                        session_code VARCHAR(50) NOT NULL,
                        user_id BIGINT NOT NULL,
                        comfyui_server_id BIGINT NOT NULL,
                        agent_code VARCHAR(50) NOT NULL,
                        agent_config CLOB NOT NULL,
                        title VARCHAR(200),
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT,
                        update_by BIGINT
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建唯一索引 (H2不支持WHERE条件,使用普通唯一索引)
            jdbcTemplate.execute("CREATE UNIQUE INDEX uk_session_code ON chat_session(session_code, is_deleted)");

            // 创建普通索引
            jdbcTemplate.execute("CREATE INDEX idx_chat_session_user_id ON chat_session(user_id)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_session_comfyui_server_id ON chat_session(comfyui_server_id)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_session_status ON chat_session(status)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_session_create_time ON chat_session(create_time)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_session_is_deleted ON chat_session(is_deleted)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_session_user_status ON chat_session(user_id, status)");

            log.info("聊天会话表创建完成");
        } else {
            log.info("聊天会话表已存在，跳过创建");
        }

        // 2. 创建chat_message表
        if (!tableExists("chat_message")) {
            log.info("开始创建聊天消息表...");
            String sql = """
                    CREATE TABLE IF NOT EXISTS chat_message (
                        id BIGINT PRIMARY KEY,
                        session_id BIGINT NOT NULL,
                        session_code VARCHAR(50) NOT NULL,
                        request_id VARCHAR(50) NOT NULL,
                        role VARCHAR(20) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                        content CLOB NOT NULL,
                        content_data CLOB,
                        metadata CLOB,
                        is_deleted BIGINT NOT NULL DEFAULT 0,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        create_by BIGINT,
                        update_by BIGINT
                    )
                    """;
            jdbcTemplate.execute(sql);

            // 创建索引
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_session_id ON chat_message(session_id)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_session_code ON chat_message(session_code)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_request_id ON chat_message(request_id)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_role ON chat_message(role)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_status ON chat_message(status)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_create_time ON chat_message(create_time)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_is_deleted ON chat_message(is_deleted)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_session_id_request_create ON chat_message(session_id, request_id, create_time)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_session_code_request_create ON chat_message(session_code, request_id, create_time)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_session_role ON chat_message(session_id, role)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_session_status ON chat_message(session_id, status)");

            log.info("聊天消息表创建完成");
        } else {
            log.info("聊天消息表已存在，跳过创建");
        }
    }

}
