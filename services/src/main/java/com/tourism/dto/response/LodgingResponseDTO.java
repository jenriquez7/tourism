package com.tourism.dto.response;

import java.util.UUID;

public record LodgingResponseDTO(UUID id,
                                 String name,
                                 String description,
                                 String information,
                                 String phone,
                                 Integer capacity,
                                 Double nightPrice,
                                 Integer stars,
                                 TouristicPlaceResponseDTO touristicPlace,
                                 Boolean enabled){}
