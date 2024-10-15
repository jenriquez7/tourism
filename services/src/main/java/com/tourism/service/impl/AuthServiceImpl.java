package com.tourism.service.impl;

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
import com.tourism.service.AuthService;
import com.tourism.util.ErrorsCode;
import com.tourism.util.MessageConstants;
import lombok.extern.slf4j.Slf4j;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncryptionService encryptionService;
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(PasswordEncryptionService encryptionService, UserRepository userRepository, LoginRepository loginRepository,
                           RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
        this.loginRepository = loginRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    public Either<ErrorDto[], Map<String, String>> login(AuthUserDto authUserDto) {
        try {
            User user = userRepository.findByEmailAndEnabled(authUserDto.getEmail(), true);
            if (user == null) {
                loginRepository.save(new Login(authUserDto.getEmail(), false));
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, ErrorsCode.LOGIN_FAILED.name(), MessageConstants.ERROR_INCORRECT_USER_OR_PASSWORD)});
            }

            if (Boolean.TRUE.equals(encryptionService.checkPassword(authUserDto.getPassword(), user.getPassword()))) {
                loginRepository.save(new Login(authUserDto.getEmail(), true));
                String accessToken = jwtTokenProvider.generateAccessToken(user);
                String refreshToken = jwtTokenProvider.generateRefreshToken(user);

                refreshTokenRepository.save(new RefreshToken(refreshToken, user));

                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);

                return Either.right(tokens);
            } else {
                loginRepository.save(new Login(authUserDto.getEmail(), false));
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, ErrorsCode.LOGIN_FAILED.name(), MessageConstants.ERROR_INCORRECT_USER_OR_PASSWORD)});
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            loginRepository.save(new Login(authUserDto.getEmail(), false));
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, ErrorsCode.LOGIN_FAILED.name(), e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Boolean> logout(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);
        if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
            refreshTokenRepository.deleteByToken(refreshToken);
            return Either.right(true);
        } else {
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, ErrorsCode.LOGOUT_FAILED.name(), MessageConstants.ERROR_INVALID_TOKEN)});
        }
    }

    @Override
    public Either<ErrorDto[], String> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveToken(request);
        if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
            RefreshToken token = refreshTokenRepository.findByToken(refreshToken);
            if (token != null) {
                User user = token.getUser();
                String newAccessToken = jwtTokenProvider.generateAccessToken(user);
                return Either.right(newAccessToken);
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, ErrorsCode.INVALID_REFRESH_TOKEN.name(), MessageConstants.ERROR_REFRESH_TOKEN_NOT_FOUND)});
            }
        } else {
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, ErrorsCode.INVALID_REFRESH_TOKEN.name())});
        }
    }

}