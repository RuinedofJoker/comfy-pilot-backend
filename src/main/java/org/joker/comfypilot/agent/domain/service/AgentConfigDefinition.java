package org.joker.comfypilot.agent.domain.service;

import org.joker.comfypilot.agent.domain.enums.AgentConfigType;

public record AgentConfigDefinition(
        String name,
        String description,
        AgentConfigType type
) {

}
