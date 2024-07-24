package com.tourism.service;

import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.response.ErrorDto;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AuthService {

    Either<ErrorDto[], Map<String, String>> login(AuthUserDto authUserDto);

    Either<ErrorDto[], Boolean> logout(HttpServletRequest request);

    Either<ErrorDto[], String> refreshToken(HttpServletRequest request);
}
