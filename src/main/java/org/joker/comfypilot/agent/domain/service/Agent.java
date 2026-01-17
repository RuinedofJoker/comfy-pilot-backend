package org.joker.comfypilot.agent.domain.service;

import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

import java.util.Map;

/**
 * Agent 接口
 * 所有 Agent 实现类都必须实现此接口
 */
public interface Agent {

    /**
     * 获取 Agent 编码（唯一标识）
     *
     * @return Agent 编码
     */
    String getAgentCode();

    /**
     * 获取 Agent 名称
     *
     * @return Agent 名称
     */
    String getAgentName();

    /**
     * 获取 Agent 描述
     *
     * @return Agent 描述
     */
    String getDescription();

    /**
     * 获取 Agent 版本号（格式：x.y.z）
     * 用于版本比较和数据库同步
     *
     * @return Agent 版本号
     */
    String getVersion();

    /**
     * 获取 Agent 配置
     * 根据该配置初始化上下文
     *
     * @return Agent Scope 配置，key-value 形式
     */
    Map<String, Object> getAgentConfig();

    /**
     * 获取 Agent Scope 配置
     * 对应 langchain4j 的 agentic-scope 概念
     * 在执行时会自动解析并注入到执行上下文中
     *
     * @return Agent Scope 配置，key-value 形式
     */
    Map<String, Object> getAgentScopeConfig();

    /**
     * 执行 Agent
     *
     * @param executionContext 执行请求上下文
     */
    void execute(AgentExecutionContext executionContext);
}
