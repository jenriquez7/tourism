package com.tourism.dto.response;

import com.tourism.model.LodgingOwner;
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
public class LodgingOwnerResponseDTO {

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;


    public static LodgingOwnerResponseDTO lodgingOwnerToResponseDto(LodgingOwner lodgingOwner) {
        return new LodgingOwnerResponseDTO(
                lodgingOwner.getId(),
                lodgingOwner.getEmail(),
                lodgingOwner.getFirstName(),
                lodgingOwner.getLastName()
        );
    }
}
