package com.tourism.dto.mappers;

import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.model.Lodging;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LodgingMapper {

    LodgingResponseDTO modelToResponseDto(Lodging lodging);
}
