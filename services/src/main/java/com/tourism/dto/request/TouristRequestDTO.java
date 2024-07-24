package com.tourism.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Create a new tourist DTO")
public class TouristRequestDTO {

    @Schema(example = "John", description = "Tourist first name")
    @Size(max = 100, message = "{validation.name.size.too_long}")
    @NotNull
    private String firstName;

    @Schema(example = "Doe", description = "Tourist last name")
    @Size(max = 100, message = "{validation.name.size.too_long}")
    @NotNull
    private String lastName;

    @Schema(example = "example@mail.com", description = "Tourist email address")
    @Size(max = 100, message = "{validation.email.size.too_long}")
    @NotNull
    private String email;

    @Schema(description = "password must contains: length 8 characters, one number, one uppercase, " +
                          "one lowercase and one special character")
    @Size(min = 8, max = 30, message = "password size must be between 8 and 30 characters")
    @NotNull
    private String password;
}
