package com.tourism.util.validations;

import com.tourism.dto.response.ErrorDto;
import com.tourism.util.MessageConstants;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserValidation {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    public Either<ErrorDto[], Boolean> validateEmailAndPassword(String email, String password) {
        List<ErrorDto> errors = new ArrayList<>();
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        if (email == null || email.isEmpty()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_EMPTY_EMAIL));
            return Either.left(errors.toArray(new ErrorDto[0]));
        }

        if (!pattern.matcher(email).matches()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_INVALID_EMAIL_FORMAT));
        }

        if (password == null || password.isEmpty()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_REQUIRED_PASSWORD));
            return Either.left(errors.toArray(new ErrorDto[0]));
        }

        if (password.length() < 8 || password.length() > 30) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_PASSWORD_LENGTH));
        }

        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_PASSWORD_UPPERCASE));
        }

        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_PASSWORD_LOWERCASE));
        }

        if (!Pattern.compile("\\d").matcher(password).find()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_PASSWORD_NUMBER));
        }

        if (!Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find()) {
            errors.add(new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_PASSWORD_SPECIAL_CHARACTER));
        }
        return errors.isEmpty() ? Either.right(true) : Either.left(errors.toArray(new ErrorDto[0]));
    }

}
