package com.tourism.dto.mappers;

import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.model.Tourist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TouristMapper {

    TouristResponseDTO modelToResponseDto(Tourist tourist);
}
