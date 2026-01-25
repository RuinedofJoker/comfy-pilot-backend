package org.joker.comfypilot.tool.infrastructure.service;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.McpSchemaUtil;
import org.joker.comfypilot.tool.domain.service.Tool;

/**
 * 客户端工具实现类
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ClientTool implements Tool {

    /**
     * 工具名称
     */
    private final String toolName;

    /**
     * MCP 工具规范
     */
    private final McpSchema.Tool toolSchema;

    /**
     * LangChain4j 工具规范（转换后）
     */
    private final ToolSpecification toolSpecification;

    /**
     * 构造客户端工具
     *
     * @param toolSchema MCP 工具规范
     */
    public ClientTool(McpSchema.Tool toolSchema) {
        this.toolSchema = toolSchema;
        this.toolName = toolSchema.name();
        this.toolSpecification = convertToToolSpecification(toolSchema);

        log.debug("创建客户端工具: name={}, description={}",
                 toolName, toolSchema.description());
    }

    /**
     * 将 MCP 工具规范转换为 LangChain4j 工具规范
     * <p>
     * 转换映射关系：
     * <ul>
     *   <li>McpSchema.Tool.name → ToolSpecification.name</li>
     *   <li>McpSchema.Tool.description → ToolSpecification.description</li>
     *   <li>McpSchema.Tool.inputSchema → ToolSpecification.parameters</li>
     * </ul>
     *
     * @param mcpTool MCP 工具规范
     * @return LangChain4j 工具规范
     * @throws BusinessException 转换失败时抛出
     */
    private ToolSpecification convertToToolSpecification(McpSchema.Tool mcpTool) {
        try {
            // 1. 获取工具名称和描述
            String name = mcpTool.name();
            String description = mcpTool.description();

            // 2. 转换输入参数 Schema
            McpSchema.JsonSchema jsonSchema = mcpTool.inputSchema();
            JsonObjectSchema parameters = McpSchemaUtil.toJsonObjectSchema(jsonSchema);

            // 3. 构建 ToolSpecification
            return ToolSpecification.builder()
                    .name(name)
                    .description(description)
                    .parameters(parameters)
                    .metadata(mcpTool.meta())
                    .build();

        } catch (Exception e) {
            log.error("转换 MCP 工具规范失败: toolName={}, error={}",
                     mcpTool.name(), e.getMessage(), e);
            throw new BusinessException("转换 MCP 工具规范失败: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toolName() {
        return toolName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToolSpecification toolSpecification() {
        return toolSpecification;
    }

    @Override
    public String executeTool(String toolCallId, String name, String arguments) {
        throw new BusinessException("客户端工具" + name + "不应该由服务端执行");
    }

}
