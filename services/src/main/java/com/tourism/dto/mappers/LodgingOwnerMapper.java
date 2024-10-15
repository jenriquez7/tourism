package com.tourism.dto.mappers;

import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.model.LodgingOwner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LodgingOwnerMapper {

    LodgingOwnerResponseDTO modelToResponseDto(LodgingOwner owner);
}
