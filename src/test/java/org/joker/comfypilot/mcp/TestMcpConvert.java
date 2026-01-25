package org.joker.comfypilot.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.request.json.*;
import io.modelcontextprotocol.spec.McpSchema;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.common.util.McpSchemaUtil;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestMcpConvert extends BaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSimpleObjectSchemaConversion() {
        // 创建一个简单的JsonObjectSchema
        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("用户信息")
                .addStringProperty("name", "用户名称")
                .addIntegerProperty("age", "用户年龄")
                .addBooleanProperty("active", "是否激活")
                .required("name", "age")
                .additionalProperties(false)
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertEquals("object", mcpSchema.type());
        assertNotNull(mcpSchema.properties());
        assertEquals(3, mcpSchema.properties().size());
        assertNotNull(mcpSchema.required());
        assertEquals(2, mcpSchema.required().size());
        assertTrue(mcpSchema.required().contains("name"));
        assertTrue(mcpSchema.required().contains("age"));
        assertEquals(false, mcpSchema.additionalProperties());

        // 反向转换回JsonObjectSchema
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);

        // 验证反向转换结果
        assertNotNull(convertedBack);
        assertEquals(3, convertedBack.properties().size());
        assertEquals(2, convertedBack.required().size());
        assertEquals(false, convertedBack.additionalProperties());
    }

    @Test
    public void testArraySchemaConversion() {
        // 创建包含数组的JsonObjectSchema
        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("用户列表")
                .addProperty("users", JsonArraySchema.builder()
                        .description("用户数组")
                        .items(JsonObjectSchema.builder()
                                .addStringProperty("name")
                                .addIntegerProperty("age")
                                .build())
                        .build())
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertNotNull(mcpSchema.properties());
        assertTrue(mcpSchema.properties().containsKey("users"));

        // 反向转换
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);
        assertNotNull(convertedBack);
        assertNotNull(convertedBack.properties());
        assertTrue(convertedBack.properties().containsKey("users"));
        assertTrue(convertedBack.properties().get("users") instanceof JsonArraySchema);
    }

    @Test
    public void testEnumSchemaConversion() {
        // 创建包含枚举的JsonObjectSchema
        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("用户状态")
                .addEnumProperty("status", List.of("ACTIVE", "INACTIVE", "PENDING"), "用户状态枚举")
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertNotNull(mcpSchema.properties());
        assertTrue(mcpSchema.properties().containsKey("status"));

        // 反向转换
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);
        assertNotNull(convertedBack);
        assertTrue(convertedBack.properties().get("status") instanceof JsonEnumSchema);
        JsonEnumSchema enumSchema = (JsonEnumSchema) convertedBack.properties().get("status");
        assertEquals(3, enumSchema.enumValues().size());
        assertTrue(enumSchema.enumValues().contains("ACTIVE"));
    }

    @Test
    public void testNestedObjectSchemaConversion() {
        // 创建嵌套对象的JsonObjectSchema
        JsonObjectSchema addressSchema = JsonObjectSchema.builder()
                .addStringProperty("street", "街道")
                .addStringProperty("city", "城市")
                .addStringProperty("country", "国家")
                .required("city", "country")
                .build();

        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("用户完整信息")
                .addStringProperty("name", "姓名")
                .addProperty("address", addressSchema)
                .required("name", "address")
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertEquals(2, mcpSchema.properties().size());

        // 反向转换
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);
        assertNotNull(convertedBack);
        assertTrue(convertedBack.properties().get("address") instanceof JsonObjectSchema);
        JsonObjectSchema addressConverted = (JsonObjectSchema) convertedBack.properties().get("address");
        assertEquals(3, addressConverted.properties().size());
        assertEquals(2, addressConverted.required().size());
    }

    @Test
    public void testAnyOfSchemaConversion() {
        // 创建包含AnyOf的JsonObjectSchema
        JsonAnyOfSchema anyOfSchema = JsonAnyOfSchema.builder()
                .description("字符串或数字")
                .anyOf(
                        new JsonStringSchema(),
                        new JsonNumberSchema()
                )
                .build();

        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("灵活类型")
                .addProperty("value", anyOfSchema)
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertNotNull(mcpSchema.properties());

        // 反向转换
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);
        assertNotNull(convertedBack);
        assertTrue(convertedBack.properties().get("value") instanceof JsonAnyOfSchema);
        JsonAnyOfSchema anyOfConverted = (JsonAnyOfSchema) convertedBack.properties().get("value");
        assertEquals(2, anyOfConverted.anyOf().size());
    }

    @Test
    public void testSchemaWithDefinitionsAndReference() {
        // 创建一个带有definitions和reference的复杂Schema
        Map<String, JsonSchemaElement> definitions = new LinkedHashMap<>();

        // 定义一个Person类型
        JsonObjectSchema personSchema = JsonObjectSchema.builder()
                .addStringProperty("name", "姓名")
                .addIntegerProperty("age", "年龄")
                .build();
        definitions.put("Person", personSchema);

        // 创建主Schema,引用Person定义
        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("团队信息")
                .addProperty("leader", JsonReferenceSchema.builder()
                        .reference("Person")
                        .build())
                .addProperty("members", JsonArraySchema.builder()
                        .items(JsonReferenceSchema.builder()
                                .reference("Person")
                                .build())
                        .build())
                .definitions(definitions)
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertNotNull(mcpSchema.properties());
        assertNotNull(mcpSchema.defs());
        assertTrue(mcpSchema.defs().containsKey("Person"));

        // 反向转换
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);
        assertNotNull(convertedBack);
        assertNotNull(convertedBack.definitions());
        assertTrue(convertedBack.definitions().containsKey("Person"));
        assertTrue(convertedBack.properties().get("leader") instanceof JsonReferenceSchema);

        JsonReferenceSchema leaderRef = (JsonReferenceSchema) convertedBack.properties().get("leader");
        assertEquals("Person", leaderRef.reference());
    }

    @Test
    public void testComplexRealWorldSchema() {
        // 创建一个接近真实场景的复杂Schema
        JsonObjectSchema langchainSchema = JsonObjectSchema.builder()
                .description("API请求参数")
                .addStringProperty("endpoint", "API端点")
                .addEnumProperty("method", List.of("GET", "POST", "PUT", "DELETE"), "HTTP方法")
                .addProperty("headers", JsonObjectSchema.builder()
                        .additionalProperties(true)
                        .build())
                .addProperty("queryParams", JsonArraySchema.builder()
                        .items(JsonObjectSchema.builder()
                                .addStringProperty("key")
                                .addStringProperty("value")
                                .required("key", "value")
                                .build())
                        .build())
                .addProperty("body", JsonAnyOfSchema.builder()
                        .anyOf(
                                new JsonStringSchema(),
                                JsonObjectSchema.builder().additionalProperties(true).build()
                        )
                        .build())
                .required("endpoint", "method")
                .additionalProperties(false)
                .build();

        // 转换为MCP Schema
        McpSchema.JsonSchema mcpSchema = McpSchemaUtil.toMcpJsonSchema(langchainSchema);

        // 验证转换结果
        assertNotNull(mcpSchema);
        assertEquals("object", mcpSchema.type());
        assertEquals(5, mcpSchema.properties().size());
        assertEquals(2, mcpSchema.required().size());

        // 反向转换
        JsonObjectSchema convertedBack = McpSchemaUtil.toJsonObjectSchema(mcpSchema);

        // 验证反向转换结果
        assertNotNull(convertedBack);
        assertEquals(5, convertedBack.properties().size());
        assertEquals(2, convertedBack.required().size());
        assertTrue(convertedBack.properties().get("method") instanceof JsonEnumSchema);
        assertTrue(convertedBack.properties().get("queryParams") instanceof JsonArraySchema);
        assertTrue(convertedBack.properties().get("body") instanceof JsonAnyOfSchema);
    }

}
