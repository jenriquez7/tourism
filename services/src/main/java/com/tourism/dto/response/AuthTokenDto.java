package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenDto {

    private String accessToken;

    private String refreshToken;

    private String scope;

    private String idToken;

    private String tokenType;

    private int expiresIn;

}
