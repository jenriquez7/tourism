package com.tourism.dto.response;

import com.tourism.model.Tourist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Tourist response DTO")
public class TouristResponseDTO {

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;


    public static TouristResponseDTO touristToResponseDto(Tourist tourist) {
        return new TouristResponseDTO(
                tourist.getId(),
                tourist.getEmail(),
                tourist.getFirstName(),
                tourist.getLastName()
        );
    }
}
