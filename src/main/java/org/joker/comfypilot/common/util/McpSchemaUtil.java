package org.joker.comfypilot.common.util;

import dev.langchain4j.model.chat.request.json.*;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.*;

/**
 * MCP Schema与Langchain4j JsonSchema相互转换工具类
 */
public final class McpSchemaUtil {

    private McpSchemaUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 将MCP JsonSchema转换为Langchain4j JsonObjectSchema
     *
     * @param mcpSchema MCP的JsonSchema
     * @return Langchain4j的JsonObjectSchema
     */
    public static JsonObjectSchema toJsonObjectSchema(McpSchema.JsonSchema mcpSchema) {
        if (mcpSchema == null) {
            return null;
        }

        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

        // 转换properties
        if (mcpSchema.properties() != null && !mcpSchema.properties().isEmpty()) {
            Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : mcpSchema.properties().entrySet()) {
                JsonSchemaElement element = convertObjectToJsonSchemaElement(entry.getValue());
                if (element != null) {
                    properties.put(entry.getKey(), element);
                }
            }
            builder.addProperties(properties);
        }

        // 转换required
        if (mcpSchema.required() != null) {
            builder.required(new ArrayList<>(mcpSchema.required()));
        }

        // 转换additionalProperties
        if (mcpSchema.additionalProperties() != null) {
            builder.additionalProperties(mcpSchema.additionalProperties());
        }

        // 转换definitions (优先使用$defs,其次使用definitions)
        Map<String, Object> defsMap = (mcpSchema.defs() != null && !mcpSchema.defs().isEmpty()) ? mcpSchema.defs() : mcpSchema.definitions();
        if (defsMap != null && !defsMap.isEmpty()) {
            Map<String, JsonSchemaElement> definitions = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : defsMap.entrySet()) {
                JsonSchemaElement element = convertObjectToJsonSchemaElement(entry.getValue());
                if (element != null) {
                    definitions.put(entry.getKey(), element);
                }
            }
            builder.definitions(definitions);
        }

        return builder.build();
    }

    /**
     * 将Object转换为JsonSchemaElement
     * MCP的properties是Map<String, Object>,需要根据Object的内容判断类型
     */
    @SuppressWarnings("unchecked")
    private static JsonSchemaElement convertObjectToJsonSchemaElement(Object obj) {
        if (obj == null) {
            return new JsonNullSchema();
        }

        // 如果是Map,需要解析其中的type字段
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            String type = (String) map.get("type");
            String description = (String) map.get("description");

            if (type == null) {
                // 检查是否有anyOf字段
                if (map.containsKey("anyOf")) {
                    return convertAnyOfSchema(map, description);
                }
                // 检查是否有$ref字段
                if (map.containsKey("$ref")) {
                    return convertReferenceSchema(map);
                }
                // 默认当作object处理
                return convertObjectSchema(map, description);
            }

            return switch (type) {
                case "string" -> convertStringSchema(map, description);
                case "integer" -> convertIntegerSchema(description);
                case "number" -> convertNumberSchema(description);
                case "boolean" -> convertBooleanSchema(description);
                case "array" -> convertArraySchema(map, description);
                case "object" -> convertObjectSchema(map, description);
                case "null" -> new JsonNullSchema();
                default -> null;
            };
        }

        return null;
    }

    /**
     * 转换String类型Schema
     */
    @SuppressWarnings("unchecked")
    private static JsonSchemaElement convertStringSchema(Map<String, Object> map, String description) {
        // 检查是否有enum字段
        if (map.containsKey("enum")) {
            List<String> enumValues = (List<String>) map.get("enum");
            return JsonEnumSchema.builder()
                    .enumValues(enumValues)
                    .description(description)
                    .build();
        }

        if (description != null) {
            return JsonStringSchema.builder().description(description).build();
        }
        return new JsonStringSchema();
    }

    /**
     * 转换Integer类型Schema
     */
    private static JsonSchemaElement convertIntegerSchema(String description) {
        if (description != null) {
            return JsonIntegerSchema.builder().description(description).build();
        }
        return new JsonIntegerSchema();
    }

    /**
     * 转换Number类型Schema
     */
    private static JsonSchemaElement convertNumberSchema(String description) {
        if (description != null) {
            return JsonNumberSchema.builder().description(description).build();
        }
        return new JsonNumberSchema();
    }

    /**
     * 转换Boolean类型Schema
     */
    private static JsonSchemaElement convertBooleanSchema(String description) {
        if (description != null) {
            return JsonBooleanSchema.builder().description(description).build();
        }
        return new JsonBooleanSchema();
    }

    /**
     * 转换Array类型Schema
     */
    @SuppressWarnings("unchecked")
    private static JsonSchemaElement convertArraySchema(Map<String, Object> map, String description) {
        JsonArraySchema.Builder builder = JsonArraySchema.builder();

        if (description != null) {
            builder.description(description);
        }

        // 转换items
        Object items = map.get("items");
        if (items != null) {
            JsonSchemaElement itemsElement = convertObjectToJsonSchemaElement(items);
            if (itemsElement != null) {
                builder.items(itemsElement);
            }
        }

        return builder.build();
    }

    /**
     * 转换Object类型Schema
     */
    @SuppressWarnings("unchecked")
    private static JsonSchemaElement convertObjectSchema(Map<String, Object> map, String description) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

        if (description != null) {
            builder.description(description);
        }

        // 转换properties
        Object propertiesObj = map.get("properties");
        if (propertiesObj instanceof Map) {
            Map<String, Object> propertiesMap = (Map<String, Object>) propertiesObj;
            Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                JsonSchemaElement element = convertObjectToJsonSchemaElement(entry.getValue());
                if (element != null) {
                    properties.put(entry.getKey(), element);
                }
            }
            builder.addProperties(properties);
        }

        // 转换required
        Object requiredObj = map.get("required");
        if (requiredObj instanceof List) {
            builder.required((List<String>) requiredObj);
        }

        // 转换additionalProperties
        Object additionalPropertiesObj = map.get("additionalProperties");
        if (additionalPropertiesObj instanceof Boolean) {
            builder.additionalProperties((Boolean) additionalPropertiesObj);
        }

        return builder.build();
    }

    /**
     * 转换AnyOf类型Schema
     */
    @SuppressWarnings("unchecked")
    private static JsonSchemaElement convertAnyOfSchema(Map<String, Object> map, String description) {
        JsonAnyOfSchema.Builder builder = JsonAnyOfSchema.builder();

        if (description != null) {
            builder.description(description);
        }

        // 转换anyOf
        Object anyOfObj = map.get("anyOf");
        if (anyOfObj instanceof List) {
            List<Object> anyOfList = (List<Object>) anyOfObj;
            List<JsonSchemaElement> anyOfElements = new ArrayList<>();
            for (Object item : anyOfList) {
                JsonSchemaElement element = convertObjectToJsonSchemaElement(item);
                if (element != null) {
                    anyOfElements.add(element);
                }
            }
            builder.anyOf(anyOfElements);
        }

        return builder.build();
    }

    /**
     * 转换Reference类型Schema
     */
    private static JsonSchemaElement convertReferenceSchema(Map<String, Object> map) {
        String ref = (String) map.get("$ref");
        if (ref != null) {
            // 移除"#/definitions/"或"#/$defs/"前缀
            String reference = ref.replace("#/definitions/", "")
                    .replace("#/$defs/", "");
            return JsonReferenceSchema.builder()
                    .reference(reference)
                    .build();
        }
        return null;
    }

    /**
     * 将Langchain4j JsonObjectSchema转换为MCP JsonSchema
     *
     * @param jsonObjectSchema Langchain4j的JsonObjectSchema
     * @return MCP的JsonSchema
     */
    public static McpSchema.JsonSchema toMcpJsonSchema(JsonObjectSchema jsonObjectSchema) {
        if (jsonObjectSchema == null) {
            return null;
        }

        Map<String, Object> properties = null;
        if (jsonObjectSchema.properties() != null && !jsonObjectSchema.properties().isEmpty()) {
            properties = new LinkedHashMap<>();
            for (Map.Entry<String, JsonSchemaElement> entry : jsonObjectSchema.properties().entrySet()) {
                Object propertySchema = convertJsonSchemaElementToObject(entry.getValue());
                if (propertySchema != null) {
                    properties.put(entry.getKey(), propertySchema);
                }
            }
        }

        List<String> required = jsonObjectSchema.required();

        Boolean additionalProperties = jsonObjectSchema.additionalProperties();

        Map<String, Object> defs = null;
        if (jsonObjectSchema.definitions() != null && !jsonObjectSchema.definitions().isEmpty()) {
            defs = new LinkedHashMap<>();
            for (Map.Entry<String, JsonSchemaElement> entry : jsonObjectSchema.definitions().entrySet()) {
                Object defSchema = convertJsonSchemaElementToObject(entry.getValue());
                if (defSchema != null) {
                    defs.put(entry.getKey(), defSchema);
                }
            }
        }

        return new McpSchema.JsonSchema("object", properties, required, additionalProperties, defs, null);
    }

    /**
     * 将JsonSchemaElement转换为Object(Map)
     */
    private static Object convertJsonSchemaElementToObject(JsonSchemaElement element) {
        if (element == null) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // 处理description
        if (element.description() != null) {
            result.put("description", element.description());
        }

        // 根据具体类型处理
        if (element instanceof JsonStringSchema) {
            result.put("type", "string");
        } else if (element instanceof JsonIntegerSchema) {
            result.put("type", "integer");
        } else if (element instanceof JsonNumberSchema) {
            result.put("type", "number");
        } else if (element instanceof JsonBooleanSchema) {
            result.put("type", "boolean");
        } else if (element instanceof JsonNullSchema) {
            result.put("type", "null");
        } else if (element instanceof JsonEnumSchema jsonEnumSchema) {
            return convertEnumSchemaToObject(jsonEnumSchema);
        } else if (element instanceof JsonArraySchema jsonArraySchema) {
            return convertArraySchemaToObject(jsonArraySchema);
        } else if (element instanceof JsonObjectSchema jsonObjectSchema) {
            return convertObjectSchemaToObject(jsonObjectSchema);
        } else if (element instanceof JsonAnyOfSchema jsonAnyOfSchema) {
            return convertAnyOfSchemaToObject(jsonAnyOfSchema);
        } else if (element instanceof JsonReferenceSchema jsonReferenceSchema) {
            return convertReferenceSchemaToObject(jsonReferenceSchema);
        }

        return result;
    }

    /**
     * 将JsonEnumSchema转换为Object(Map)
     */
    private static Object convertEnumSchemaToObject(JsonEnumSchema enumSchema) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "string");
        result.put("enum", new ArrayList<>(enumSchema.enumValues()));
        if (enumSchema.description() != null) {
            result.put("description", enumSchema.description());
        }
        return result;
    }

    /**
     * 将JsonArraySchema转换为Object(Map)
     */
    private static Object convertArraySchemaToObject(JsonArraySchema arraySchema) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "array");
        if (arraySchema.description() != null) {
            result.put("description", arraySchema.description());
        }
        if (arraySchema.items() != null) {
            result.put("items", convertJsonSchemaElementToObject(arraySchema.items()));
        }
        return result;
    }

    /**
     * 将JsonObjectSchema转换为Object(Map)
     */
    private static Object convertObjectSchemaToObject(JsonObjectSchema objectSchema) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "object");

        if (objectSchema.description() != null) {
            result.put("description", objectSchema.description());
        }

        if (objectSchema.properties() != null && !objectSchema.properties().isEmpty()) {
            Map<String, Object> properties = new LinkedHashMap<>();
            for (Map.Entry<String, JsonSchemaElement> entry : objectSchema.properties().entrySet()) {
                Object propertySchema = convertJsonSchemaElementToObject(entry.getValue());
                if (propertySchema != null) {
                    properties.put(entry.getKey(), propertySchema);
                }
            }
            result.put("properties", properties);
        }

        if (objectSchema.required() != null && !objectSchema.required().isEmpty()) {
            result.put("required", new ArrayList<>(objectSchema.required()));
        }

        if (objectSchema.additionalProperties() != null) {
            result.put("additionalProperties", objectSchema.additionalProperties());
        }

        return result;
    }

    /**
     * 将JsonAnyOfSchema转换为Object(Map)
     */
    private static Object convertAnyOfSchemaToObject(JsonAnyOfSchema anyOfSchema) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (anyOfSchema.description() != null) {
            result.put("description", anyOfSchema.description());
        }

        if (anyOfSchema.anyOf() != null && !anyOfSchema.anyOf().isEmpty()) {
            List<Object> anyOfList = new ArrayList<>();
            for (JsonSchemaElement element : anyOfSchema.anyOf()) {
                Object elementSchema = convertJsonSchemaElementToObject(element);
                if (elementSchema != null) {
                    anyOfList.add(elementSchema);
                }
            }
            result.put("anyOf", anyOfList);
        }

        return result;
    }

    /**
     * 将JsonReferenceSchema转换为Object(Map)
     */
    private static Object convertReferenceSchemaToObject(JsonReferenceSchema referenceSchema) {
        Map<String, Object> result = new LinkedHashMap<>();
        // 添加"#/$defs/"前缀
        result.put("$ref", "#/$defs/" + referenceSchema.reference());
        return result;
    }

}

