package org.sguesdon.api.jsonvalidator.models.domain.mapper;

import org.mapstruct.Mapper;
import org.sguesdon.api.jsonvalidator.models.domain.entity.ModelDto;
import org.sguesdon.api.jsonvalidator.openapi.model.Model;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ModelMapper {
    Model fromDto(ModelDto dto);
    List<Model> fromDtos(List<ModelDto> dto);
    ModelDto toDto(Model model);
    List<ModelDto> toDtos(List<Model> models);
}
