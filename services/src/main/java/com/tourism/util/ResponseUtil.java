package com.tourism.util;

import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.MetaDto;
import com.tourism.dto.response.StandardResponseDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> StandardResponseDto<T> build(HttpServletRequest request, T data) {
        StandardResponseDto<T> response = createStandardResponse(request);

        if (data != null && !data.getClass().isArray()) {
            response.setData((T[]) List.of(data).toArray());
        } else {
            response.setData((T[]) data);
        }

        return response;
    }

    public static <T> StandardResponseDto<T> build(HttpServletRequest request, T[] data) {
        StandardResponseDto<T> response = createStandardResponse(request);

        if (data != null) {
            response.setData(data);
        }

        return response;
    }

    public static <T> StandardResponseDto<T> build(HttpServletRequest request, ErrorDto[] errors) {
        StandardResponseDto<T> response = createStandardResponse(request);
        response.setErrors(errors);

        return response;
    }

    public static <T> StandardResponseDto<T> build(String method, String contextPath, ErrorDto[] errors) {
        StandardResponseDto<T> response = createStandardResponse(method, contextPath);
        response.setErrors(errors);

        return response;
    }

    public static MetaDto builMeta(HttpServletRequest request) {
        return createMeta(request);
    }

    private static <T> StandardResponseDto<T> createStandardResponse(HttpServletRequest request) {
        StandardResponseDto<T> response = new StandardResponseDto<>();
        response.setMeta(createMeta(request));
        return response;
    }

    private static <T> StandardResponseDto<T> createStandardResponse(String method, String contextPath) {
        StandardResponseDto<T> response = new StandardResponseDto<>();
        response.setMeta(createMeta(method, contextPath));
        return response;
    }

    public static MetaDto createMeta(HttpServletRequest request) {
        return createMeta(request.getMethod(), request.getRequestURI());
    }

    private static MetaDto createMeta(String method, String contextPath) {
        var meta = new MetaDto();
        meta.setMethod(method);
        meta.setOperation(contextPath);

        return meta;
    }
}
