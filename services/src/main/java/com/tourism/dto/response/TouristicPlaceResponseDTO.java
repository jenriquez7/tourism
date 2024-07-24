package com.tourism.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourism.model.Region;
import com.tourism.model.TouristicPlace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TouristicPlaceResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private Region region;
    @JsonProperty("categories")
    private List<CategoryDTO> categoryDTOs;
    private Boolean enabled;

    public static TouristicPlaceResponseDTO touristicPlaceToResponseDTO(TouristicPlace touristicPlace) {
        return new TouristicPlaceResponseDTO(
                touristicPlace.getId(),
                touristicPlace.getName(),
                touristicPlace.getDescription(),
                touristicPlace.getRegion(),
                touristicPlace.getCategories().stream().map(tpc -> new CategoryDTO(tpc.getCategory())).toList(),
                touristicPlace.getEnabled()
        );
    }
}
