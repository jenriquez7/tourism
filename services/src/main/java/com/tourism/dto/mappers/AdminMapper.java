package com.tourism.dto.mappers;

import com.tourism.dto.response.AdminResponseDTO;
import com.tourism.model.Admin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminResponseDTO modelToResponseDto(Admin admin);
}
