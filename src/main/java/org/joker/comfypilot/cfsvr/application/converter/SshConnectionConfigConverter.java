package org.joker.comfypilot.cfsvr.application.converter;

import org.joker.comfypilot.cfsvr.application.dto.SshConnectionConfigDTO;
import org.joker.comfypilot.cfsvr.domain.entity.SshConnectionConfig;
import org.mapstruct.Mapper;

/**
 * SSH连接配置转换器
 */
@Mapper(componentModel = "spring")
public interface SshConnectionConfigConverter {

    /**
     * DTO转Entity
     */
    SshConnectionConfig toEntity(SshConnectionConfigDTO dto);

    /**
     * Entity转DTO
     */
    SshConnectionConfigDTO toDTO(SshConnectionConfig entity);
}
