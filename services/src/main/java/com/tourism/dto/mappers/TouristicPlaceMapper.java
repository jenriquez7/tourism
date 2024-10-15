package com.tourism.dto.mappers;

import com.tourism.dto.response.CategoryDTO;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.model.TouristicPlace;
import com.tourism.model.TouristicPlaceCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TouristicPlaceMapper {

    @Mapping(target = "categoryDTOs", source = "categories")
    TouristicPlaceResponseDTO modelToResponseDto(TouristicPlace place);

    default List<CategoryDTO> mapCategories(List<TouristicPlaceCategory> categories) {
        return categories.stream()
                .map(tpc -> new CategoryDTO(tpc.getCategory().getId(), tpc.getCategory().getName()))
                .collect(Collectors.toList());
    }
}
