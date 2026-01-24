package org.joker.comfypilot.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.util.FileContentUtil;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.WebSocketMessageData;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class TestJsonParse extends BaseTest {

    @Test
    public void testJsonParse() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = """
                {
                  "type": "USER_MESSAGE",
                  "sessionCode": "session_8c03c2823aae40519d611098119abae0",
                  "requestId": "1769259872198",
                  "content": "你好，我测试一下agent",
                  "data": {
                    "type": "USER_MESSAGE",
                    "workflowContent": "{\\n  \\"id\\": \\"ef0a62f9-18ca-4e40-9769-f9c009d02ace\\",\\n  \\"revision\\": 0,\\n  \\"last_node_id\\": 0,\\n  \\"last_link_id\\": 0,\\n  \\"nodes\\": [],\\n  \\"links\\": [],\\n  \\"groups\\": [],\\n  \\"config\\": {},\\n  \\"extra\\": {\\n    \\"workflowRendererVersion\\": \\"LG\\",\\n    \\"ds\\": {\\n      \\"scale\\": 0.6830134553650705,\\n      \\"offset\\": [\\n        -139.8907,\\n        816.1587000000002\\n      ]\\n    }\\n  },\\n  \\"version\\": 0.4\\n}",
                    "toolSchemas": [
                      {
                        "name": "execute_workflow",
                        "description": "执行当前 ComfyUI 工作流并返回结果",
                        "inputSchema": {
                          "type": "object",
                          "properties": {
                            "batchCount": {
                              "type": "number",
                              "description": "批次数量",
                              "default": 1
                            }
                          }
                        }
                      },
                      {
                        "name": "get_workflow",
                        "description": "获取当前工作流的 JSON 内容",
                        "inputSchema": {
                          "type": "object",
                          "properties": {}
                        }
                      },
                      {
                        "name": "set_workflow",
                        "description": "设置工作流内容",
                        "inputSchema": {
                          "type": "object",
                          "properties": {
                            "workflow": {
                              "type": "object",
                              "description": "工作流 JSON 对象"
                            }
                          },
                          "required": [
                            "workflow"
                          ]
                        }
                      },
                      {
                        "name": "load_workflow",
                        "description": "在 ComfyUI 中加载工作流",
                        "inputSchema": {
                          "type": "object",
                          "properties": {
                            "content": {
                              "type": "string",
                              "description": "工作流 JSON 字符串"
                            }
                          },
                          "required": [
                            "content"
                          ]
                        }
                      }
                    ]
                  },
                  "timestamp": 1769259872198
                }
                """;
        // 反序列化测试
        WebSocketMessage<? extends WebSocketMessageData> wsMessage =
                objectMapper.readValue(payload, new TypeReference<WebSocketMessage<WebSocketMessageData>>() {});

        // 验证反序列化结果
        log.info("反序列化成功: type={}, sessionCode={}, data类型={}",
                wsMessage.getType(),
                wsMessage.getSessionCode(),
                wsMessage.getData().getClass().getSimpleName());

        // 序列化测试
        String serialized = objectMapper.writeValueAsString(wsMessage);
        log.info("序列化结果: {}", serialized);
    }

    @Test
    public void testJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // 1. 演示 UserMessage 的序列化和反序列化
        System.out.println("========== 1. UserMessage 序列化演示 ==========");
        UserMessage userMessage = UserMessage.from(List.of(
                TextContent.from("帮我总结一下这个音频的内容"),
                AudioContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\mlk.flac"), FileContentUtil.getMimeType("C:\\Users\\61640\\Desktop\\mlk.flac")),
                ImageContent.from("https://ir78450cc343.vicp.fun/微信图片_20260103154248_7584_34.jpg"),
                VideoContent.from("https://ir78450cc343.vicp.fun/35507798632-1-192.mp4"),
                ImageContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\微信图片_20260103154248_7584_34.jpg"), "image/jpeg")
        ));

        // 转换为 PersistableChatMessage
        PersistableChatMessage persistableMsg =
                PersistableChatMessage.from(userMessage);

        // 序列化为 JSON
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(persistableMsg);
        System.out.println("序列化结果：");
        System.out.println(json);
        System.out.println();

        // 反序列化
        PersistableChatMessage restored =
                mapper.readValue(json, PersistableChatMessage.class);
        System.out.println("反序列化成功，类型：" + restored.getClass().getSimpleName());
        System.out.println();
    }

}
