package com.tourism.dto.mappers;

import com.tourism.dto.response.CategoryDTO;
import com.tourism.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDTO modelToResponseDTO(Category category);
}
