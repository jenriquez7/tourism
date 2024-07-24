package com.tourism.dto.response;

import com.tourism.model.Admin;
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
@Schema(description = "Admin's data response DTO")
public class AdminResponseDTO {

    private UUID id;

    private String email;


    public static AdminResponseDTO adminToResponseDto(Admin admin) {
        return new AdminResponseDTO(admin.getId(), admin.getEmail());
    }
}
