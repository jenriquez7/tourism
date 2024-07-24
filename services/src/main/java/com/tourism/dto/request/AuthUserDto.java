package com.tourism.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User's data login DTO")
public class AuthUserDto {

    @Schema(example = "example@mail.com", description = "The user's email address")
    @Size(max = 100, message = "{validation.name.size.too_long}")
    @NotNull
    private String email;

    @Schema(description = "password must contains: length 8 characters, one number, one uppercase, " +
                          "one lowercase and one special character")
    @Size(min = 8, max = 30, message = "password size must be between 8 and 30 characters")
    @NotNull
    private String password;
}
