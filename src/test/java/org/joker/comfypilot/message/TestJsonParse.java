package org.joker.comfypilot.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.WebSocketMessageData;
import org.junit.jupiter.api.Test;

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
        WebSocketMessage<? extends WebSocketMessageData> wsMessage =
                objectMapper.readValue(payload, new TypeReference<WebSocketMessage<WebSocketMessageData>>() {});
    }

}
