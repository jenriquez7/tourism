package com.tourism.controller;

import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.service.AuthService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.AUTH_PATH,
                produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@Validated
public class AuthController {

    private final AuthService service;

    @Autowired
    public AuthController(AuthService service) {
        this.service = service;
    }


    @Operation(summary = "EndPoint that performs a user login and returns the authentication token, id token and refresh token.",
            operationId = "login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Error when logging in, for details see response errors"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = EndpointConstants.LOGIN_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PermitAll
    public ResponseEntity<StandardResponseDto<Map<String, String>>> login(HttpServletRequest request, @RequestBody @Valid AuthUserDto authUserDto) {
        return ResponseEntityUtil.buildObject(request, service.login(authUserDto));
    }


    @Operation(summary = "Endpoint to refresh the access token using a valid refresh token.", operationId = "refreshToken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = EndpointConstants.REFRESH_PATH)
    @PermitAll
    public ResponseEntity<StandardResponseDto<String>> refreshToken(HttpServletRequest request) {
        return ResponseEntityUtil.buildObject(request, service.refreshToken(request));

    }


    @Operation(summary = "EndPoint that performs a user login and returns the authentication token, id token and refresh token.",
            operationId = "login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Error when logging in, for details see response errors"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = EndpointConstants.LOGOUT_PATH)
    @PermitAll
    public  ResponseEntity<StandardResponseDto<Boolean>> logout(HttpServletRequest request) {
        return ResponseEntityUtil.buildObject(request, service.logout(request));
    }
}
