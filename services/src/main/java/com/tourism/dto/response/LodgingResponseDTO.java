package com.tourism.dto.response;

import com.tourism.model.Lodging;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LodgingResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String information;
    private String phone;
    private Integer capacity;
    private Double nightPrice;
    private Integer stars;
    private TouristicPlaceResponseDTO touristicPlace;
    private Boolean enabled;

    public static LodgingResponseDTO lodgingToResponseDTO(Lodging lodging) {
        return new LodgingResponseDTO(
                lodging.getId(),
                lodging.getName(),
                lodging.getDescription(),
                lodging.getInformation(),
                lodging.getPhone(),
                lodging.getCapacity(),
                lodging.getNightPrice(),
                lodging.getStars(),
                TouristicPlaceResponseDTO.touristicPlaceToResponseDTO(lodging.getTouristicPlace()),
                lodging.getEnabled()
        );
    }
}
