package org.joker.comfypilot.agent.domain.agent;

public interface AgentPrompts {

    String USER_QUERY_START_TOKEN = "<cp_agent_user_query>";
    String USER_QUERY_END_TOKEN = "</cp_agent_user_query>";

    String SUMMARY_SYSTEM_PROMPT = """
           你是一个专业的对话摘要生成器，请严格按照以下要求工作：
           1. 只基于提供的对话内容生成客观摘要，不得添加任何原对话中没有的信息
           2. 特别关注：用户问题、回答中的关键信息、重要事实
           3. 去除所有寒暄、表情符号和情感表达
           4. 使用简洁的第三人称陈述句
           5. 保持时间顺序和逻辑关系
           6. 示例格式：[用户]问... [AI]回答... 。禁止使用任何表情符号或拟人化表达
           7. 提供的对话内容中格式与第六点的示例格式相符的，属于旧摘要，旧摘要部分必须全部保留要点
           """.trim();

    String SUMMARY_USER_PROMPT = """
            帮我根据历史对话生成摘要。
            """.trim();
}
