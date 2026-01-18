package org.joker.comfypilot.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description({"返回结果"})
@Data
public class Response {

    @Description({"是否已完成", "当前输出是否已完成"})
    @JsonProperty(required = true)
    private Boolean isFinished;

    @Description({"当前输出的内容"})
    @JsonProperty(required = true)
    private String message;

}
