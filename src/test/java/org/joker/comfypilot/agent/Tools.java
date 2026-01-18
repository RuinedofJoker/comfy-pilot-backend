package org.joker.comfypilot.agent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class Tools {
    @Tool(name = "joke", value = "调用该工具会生成一个关于指定名字的笑话")
    public String joke(@P("名字") String name, @P("这个名字属不属于ai") boolean isAi) {
        return "哈哈你被耍了" + name + "!";
    }
}
