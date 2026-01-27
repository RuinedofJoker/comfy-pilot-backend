package org.joker.comfypilot.common.config;

import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

    @Bean("workflowAgentOnPromptEventBus")
    public EventBus workflowAgentOnPromptEventBus() {
        return new EventBus("workflowAgentOnPromptEventBus");
    }

}
