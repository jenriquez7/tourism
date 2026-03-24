package com.tourism.util;

import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.StandardResponseDto;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

@Slf4j
public class ResponseEntityUtil {

    public static final String RESPONSE_LOG_INFO = "Response: {}";

    private ResponseEntityUtil() {}

    public static <T> ResponseEntity<StandardResponseDto<T>> buildArray(HttpServletRequest request, Either<ErrorDto[], T[]> responseEither) {
        HttpStatus status = HttpStatus.OK;
        log.info(RESPONSE_LOG_INFO, responseEither);
        StandardResponseDto<T> responseDto;

        if (responseEither.isLeft() && Arrays.stream(responseEither.getLeft()).findFirst().isPresent()) {
            status = Arrays.stream(responseEither.getLeft()).findFirst().get().code();
            responseDto = ResponseUtil.build(request, responseEither.getLeft());
        } else {
            responseDto = ResponseUtil.build(request, responseEither.get());
        }

        return new ResponseEntity<>(responseDto, status);
    }

    public static <T> ResponseEntity<StandardResponseDto<T>> buildObject(HttpServletRequest request, Either<ErrorDto[], T> responseEither) {
        HttpStatus status = HttpStatus.OK;
        log.info(RESPONSE_LOG_INFO, responseEither);
        StandardResponseDto<T> responseDto;

        if (responseEither.isLeft() && Arrays.stream(responseEither.getLeft()).findFirst().isPresent()) {
            status = Arrays.stream(responseEither.getLeft()).findFirst().get().code();
            responseDto = ResponseUtil.build(request, responseEither.getLeft());
        } else {
            responseDto = ResponseUtil.build(request, responseEither.get());
        }

        return new ResponseEntity<>(responseDto, status);
    }

    public static <T> ResponseEntity<StandardResponseDto<T>> build(HttpServletRequest request, Either<ErrorDto, T> responseEither) {
        Either<ErrorDto[], T> tmpEither;

        if (responseEither.isRight())
            tmpEither = Either.right(responseEither.get());
        else
            tmpEither = Either.left(new ErrorDto[]{responseEither.getLeft()});

        return buildObject(request, tmpEither);
    }

    public static <T> ResponseEntity<StandardResponseDto<T>> buildStandardResponse(HttpServletRequest request, Either<ErrorDto[], StandardResponseDto<T>> responseEither) {
        HttpStatus status = HttpStatus.OK;
        log.info(RESPONSE_LOG_INFO, responseEither);
        StandardResponseDto<T> responseDto;

        if (responseEither.isLeft() && Arrays.stream(responseEither.getLeft()).findFirst().isPresent()) {
            status = Arrays.stream(responseEither.getLeft()).findFirst().get().code();
            responseDto = ResponseUtil.build(request, responseEither.getLeft());
        } else {
            var meta = ResponseUtil.createMeta(request);
            var responseEitherDto = responseEither.get();
            meta.setPaging(responseEitherDto.getMeta().getPaging());

            responseDto = responseEitherDto;
        }

        return new ResponseEntity<>(responseDto, status);
    }

    public static <T> ResponseEntity<StandardResponseDto<T>> buildErrorArray(HttpServletRequest request, Either<ErrorDto[], T> responseEither) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.info(RESPONSE_LOG_INFO, responseEither);
        StandardResponseDto<T> responseDto;

        if (responseEither.isLeft() && Arrays.stream(responseEither.getLeft()).findFirst().isPresent()) {
            status = Arrays.stream(responseEither.getLeft()).findFirst().get().code();
            responseDto = ResponseUtil.build(request, responseEither.getLeft());
        } else {
            responseDto = ResponseUtil.build(request, responseEither.get());
        }

        return new ResponseEntity<>(responseDto, status);
    }

    public static <T> ResponseEntity<StandardResponseDto<T>> buildArrayErrorArray(HttpServletRequest request, Either<ErrorDto[], T[]> responseEither) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.info(RESPONSE_LOG_INFO, responseEither);
        StandardResponseDto<T> responseDto;

        if (responseEither.isLeft() && Arrays.stream(responseEither.getLeft()).findFirst().isPresent()) {
            status = Arrays.stream(responseEither.getLeft()).findFirst().get().code();
            responseDto = ResponseUtil.build(request, responseEither.getLeft());
        } else {
            responseDto = ResponseUtil.build(request, responseEither.get());
        }

        return new ResponseEntity<>(responseDto, status);
    }
}
