package com.tourism.test.controller;

import com.tourism.controller.AuthController;
import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.service.AuthService;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    @Mock
    private AuthService service;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController controller;

    private Map<String, String> authResponse;
    private AuthUserDto authUserDto;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        authUserDto = new AuthUserDto("email@email.com", "12345678");
        authResponse = new HashMap<>();
        authResponse.put("accessToken", "aToken");
        authResponse.put("refreshToken", "rToken");
        refreshToken = "refreshValue";
    }



    @Test
    @DisplayName("Login")
    void create() {
        when(service.login(any(AuthUserDto.class))).thenReturn(Either.right(authResponse));
        ResponseEntity<StandardResponseDto<Map<String, String>>> response = controller.login(request, authUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Map<String, String>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Map.class, data[0]);

        Map<String, String> tokenMap = (Map<String, String>) data[0];
        assertEquals("aToken", tokenMap.get("accessToken"));
        assertEquals("rToken", tokenMap.get("refreshToken"));
        verify(service, times(1)).login(any(AuthUserDto.class));
    }

    @Test
    @DisplayName("Refresh Token")
    void refreshToken() {
        when(service.refreshToken(any(HttpServletRequest.class))).thenReturn(Either.right(refreshToken));
        ResponseEntity<StandardResponseDto<String>> response = controller.refreshToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<String> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(String.class, data[0]);

        String refreshTokenResult = (String) data[0];
        assertEquals(refreshToken, refreshTokenResult);

        verify(service, times(1)).refreshToken(any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Logout")
    void logout() {
        when(service.logout(any(HttpServletRequest.class))).thenReturn(Either.right(true));
        ResponseEntity<StandardResponseDto<Boolean>> response = controller.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Boolean> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Boolean.class, data[0]);

        Boolean refreshTokenResult = (Boolean) data[0];
        assertEquals(true, refreshTokenResult);

        verify(service, times(1)).logout(any(HttpServletRequest.class));
    }
}
