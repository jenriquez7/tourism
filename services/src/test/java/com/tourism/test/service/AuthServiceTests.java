package com.tourism.test.service;

import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.response.ErrorDto;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Login;
import com.tourism.model.RefreshToken;
import com.tourism.model.User;
import com.tourism.repository.LoginRepository;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.impl.AuthServiceImpl;
import com.tourism.util.ErrorsCode;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private PasswordEncryptionService encryptionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Login - Success")
    void loginSuccess() {
        AuthUserDto authUserDto = new AuthUserDto("test@example.com", "password");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmailAndEnabled(authUserDto.getEmail(), true)).thenReturn(user);
        when(encryptionService.checkPassword(authUserDto.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refreshToken");

        Either<ErrorDto[], Map<String, String>> result = authService.login(authUserDto);

        assertTrue(result.isRight());
        Map<String, String> tokens = result.get();
        assertEquals("accessToken", tokens.get("accessToken"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
        verify(loginRepository).save(any(Login.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login - Incorrect Password")
    void loginIncorrectPassword() {
        AuthUserDto authUserDto = new AuthUserDto("test@example.com", "wrongPassword");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmailAndEnabled(authUserDto.getEmail(), true)).thenReturn(user);
        when(encryptionService.checkPassword(authUserDto.getPassword(), user.getPassword())).thenReturn(false);

        Either<ErrorDto[], Map<String, String>> result = authService.login(authUserDto);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(ErrorsCode.LOGIN_FAILED.name(), errors[0].message());
        verify(loginRepository).save(any(Login.class));
    }

    @Test
    @DisplayName("Login - Exception")
    void loginException() {
        AuthUserDto authUserDto = new AuthUserDto("test@example.com", "password");

        when(userRepository.findByEmailAndEnabled(authUserDto.getEmail(), true)).thenThrow(new RuntimeException("Database error"));

        Either<ErrorDto[], Map<String, String>> result = authService.login(authUserDto);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(ErrorsCode.LOGIN_FAILED.name(), errors[0].message());
        assertEquals("Database error", errors[0].detail());
        verify(loginRepository).save(any(Login.class));
    }

    @Test
    @DisplayName("Logout - Success")
    void logoutSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String refreshToken = "validRefreshToken";

        when(jwtTokenProvider.resolveToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);

        Either<ErrorDto[], Boolean> result = authService.logout(request);

        assertTrue(result.isRight());
        assertTrue(result.get());
        verify(refreshTokenRepository).deleteByToken(refreshToken);
    }

    @Test
    @DisplayName("Logout - Invalid Token")
    void logoutInvalidToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String refreshToken = "invalidRefreshToken";

        when(jwtTokenProvider.resolveToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        Either<ErrorDto[], Boolean> result = authService.logout(request);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(ErrorsCode.LOGOUT_FAILED.name(), errors[0].message());
    }

    @Test
    @DisplayName("Refresh Token - Success")
    void refreshTokenSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String refreshToken = "validRefreshToken";
        User user = new User();
        RefreshToken token = new RefreshToken(refreshToken, user);

        when(jwtTokenProvider.resolveToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(token);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("newAccessToken");

        Either<ErrorDto[], String> result = authService.refreshToken(request);

        assertTrue(result.isRight());
        assertEquals("newAccessToken", result.get());
    }

    @Test
    @DisplayName("Refresh Token - Token Not Found")
    void refreshTokenNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String refreshToken = "validButNotFoundRefreshToken";

        when(jwtTokenProvider.resolveToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(null);

        Either<ErrorDto[], String> result = authService.refreshToken(request);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals("INVALID_REFRESH_TOKEN", errors[0].message());
    }

    @Test
    @DisplayName("Refresh Token - Invalid Token")
    void refreshTokenInvalid() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String refreshToken = "invalidRefreshToken";

        when(jwtTokenProvider.resolveToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        Either<ErrorDto[], String> result = authService.refreshToken(request);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals("INVALID_REFRESH_TOKEN", errors[0].message());
    }
}
