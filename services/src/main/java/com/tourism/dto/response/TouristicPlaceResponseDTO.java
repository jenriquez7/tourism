package com.tourism.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourism.model.Region;

import java.util.List;
import java.util.UUID;

public record TouristicPlaceResponseDTO (UUID id,
                                         String name,
                                         String description,
                                         Region region,
                                         @JsonProperty("categories")
                                         List<CategoryDTO> categoryDTOs,
                                         Boolean enabled) { }
