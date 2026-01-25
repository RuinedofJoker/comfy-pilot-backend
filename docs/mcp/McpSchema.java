/*
 * Copyright 2024-2024 the original author or authors.
 */

package io.modelcontextprotocol.spec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on the <a href="http://www.jsonrpc.org/specification">JSON-RPC 2.0
 * specification</a> and the <a href=
 * "https://github.com/modelcontextprotocol/specification/blob/main/schema/2024-11-05/schema.ts">Model
 * Context Protocol Schema</a>.
 *
 * @author Christian Tzolov
 * @author Luca Chang
 * @author Surbhi Bansal
 * @author Anurag Pant
 */
public final class McpSchema {

    private static final Logger logger = LoggerFactory.getLogger(McpSchema.class);

    private McpSchema() {
    }

    @Deprecated
    public static final String LATEST_PROTOCOL_VERSION = ProtocolVersions.MCP_2025_06_18;

    public static final String JSONRPC_VERSION = "2.0";

    public static final String FIRST_PAGE = null;

    // ---------------------------
    // Method Names
    // ---------------------------

    // Lifecycle Methods
    public static final String METHOD_INITIALIZE = "initialize";

    public static final String METHOD_NOTIFICATION_INITIALIZED = "notifications/initialized";

    public static final String METHOD_PING = "ping";

    public static final String METHOD_NOTIFICATION_PROGRESS = "notifications/progress";

    // Tool Methods
    public static final String METHOD_TOOLS_LIST = "tools/list";

    public static final String METHOD_TOOLS_CALL = "tools/call";

    public static final String METHOD_NOTIFICATION_TOOLS_LIST_CHANGED = "notifications/tools/list_changed";

    // Resources Methods
    public static final String METHOD_RESOURCES_LIST = "resources/list";

    public static final String METHOD_RESOURCES_READ = "resources/read";

    public static final String METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED = "notifications/resources/list_changed";

    public static final String METHOD_NOTIFICATION_RESOURCES_UPDATED = "notifications/resources/updated";

    public static final String METHOD_RESOURCES_TEMPLATES_LIST = "resources/templates/list";

    public static final String METHOD_RESOURCES_SUBSCRIBE = "resources/subscribe";

    public static final String METHOD_RESOURCES_UNSUBSCRIBE = "resources/unsubscribe";

    // Prompt Methods
    public static final String METHOD_PROMPT_LIST = "prompts/list";

    public static final String METHOD_PROMPT_GET = "prompts/get";

    public static final String METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED = "notifications/prompts/list_changed";

    public static final String METHOD_COMPLETION_COMPLETE = "completion/complete";

    // Logging Methods
    public static final String METHOD_LOGGING_SET_LEVEL = "logging/setLevel";

    public static final String METHOD_NOTIFICATION_MESSAGE = "notifications/message";

    // Roots Methods
    public static final String METHOD_ROOTS_LIST = "roots/list";

    public static final String METHOD_NOTIFICATION_ROOTS_LIST_CHANGED = "notifications/roots/list_changed";

    // Sampling Methods
    public static final String METHOD_SAMPLING_CREATE_MESSAGE = "sampling/createMessage";

    // Elicitation Methods
    public static final String METHOD_ELICITATION_CREATE = "elicitation/create";

    // ---------------------------
    // JSON-RPC Error Codes
    // ---------------------------
    /**
     * Standard error codes used in MCP JSON-RPC responses.
     */
    public static final class ErrorCodes {

        /**
         * Invalid JSON was received by the server.
         */
        public static final int PARSE_ERROR = -32700;

        /**
         * The JSON sent is not a valid Request object.
         */
        public static final int INVALID_REQUEST = -32600;

        /**
         * The method does not exist / is not available.
         */
        public static final int METHOD_NOT_FOUND = -32601;

        /**
         * Invalid method parameter(s).
         */
        public static final int INVALID_PARAMS = -32602;

        /**
         * Internal JSON-RPC error.
         */
        public static final int INTERNAL_ERROR = -32603;

        /**
         * Resource not found.
         */
        public static final int RESOURCE_NOT_FOUND = -32002;

    }

    /**
     * Base interface for MCP objects that include optional metadata in the `_meta` field.
     */
    public interface Meta {

        /**
         * @see <a href=
         * "https://modelcontextprotocol.io/specification/2025-06-18/basic/index#meta">Specification</a>
         * for notes on _meta usage
         * @return additional metadata related to this resource.
         */
        Map<String, Object> meta();

    }

    // ..省略了很多代码

    /**
     * A JSON Schema object that describes the expected structure of arguments or output.
     *
     * @param type The type of the schema (e.g., "object")
     * @param properties The properties of the schema object
     * @param required List of required property names
     * @param additionalProperties Whether additional properties are allowed
     * @param defs Schema definitions using the newer $defs keyword
     * @param definitions Schema definitions using the legacy definitions keyword
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JsonSchema( // @formatter:off
                              @JsonProperty("type") String type,
                              @JsonProperty("properties") Map<String, Object> properties,
                              @JsonProperty("required") List<String> required,
                              @JsonProperty("additionalProperties") Boolean additionalProperties,
                              @JsonProperty("$defs") Map<String, Object> defs,
                              @JsonProperty("definitions") Map<String, Object> definitions) { // @formatter:on
    }

    // ..省略了很多代码


}