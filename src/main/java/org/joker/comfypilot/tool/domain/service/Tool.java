package org.joker.comfypilot.tool.domain.service;

import dev.langchain4j.agent.tool.ToolSpecification;

import java.lang.reflect.Method;

public interface Tool {

    String toolName();

    Method method();

    Object instance();

    ToolSpecification toolSpecification();

    String executeTool(String name, String arguments);

}
