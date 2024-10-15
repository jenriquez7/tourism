package com.tourism.test.util;

import com.tourism.dto.response.ErrorDto;
import com.tourism.util.MessageConstants;
import com.tourism.util.validations.UserValidation;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Service
class UserValidationTests {

    @InjectMocks
    private UserValidation userValidation;

    private String validEmail;
    private String validPassword;

    @BeforeEach
    void setUp() {
        userValidation = new UserValidation();
        validEmail = "test@example.com";
        validPassword = "Valid@Password1";
    }

    @Test
    @DisplayName("Validate Email and Password - Valid Input")
    void testValidateEmailAndPasswordValidInput() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, validPassword);
        assertTrue(result.isRight());
        assertTrue(result.get());
    }

    @Test
    @DisplayName("Validate Email and Password - Null Email")
    void testValidateEmailAndPasswordNullEmail() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(null, validPassword);
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_EMPTY_EMAIL, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - Empty Email")
    void testValidateEmailAndPasswordEmptyEmail() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword("", validPassword);
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_EMPTY_EMAIL, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - Invalid Email Format")
    void testValidateEmailAndPasswordInvalidEmailFormat() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword("invalid-email", validPassword);
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_INVALID_EMAIL_FORMAT, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - Null Password")
    void testValidateEmailAndPasswordNullPassword() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, null);
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_REQUIRED_PASSWORD, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - Empty Password")
    void testValidateEmailAndPasswordEmptyPassword() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_REQUIRED_PASSWORD, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - Short Password")
    void testValidateEmailAndPasswordShortPassword() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "Short1!");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_PASSWORD_LENGTH, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - Long Password")
    void testValidateEmailAndPasswordLongPassword() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "ThisPasswordIsWayTooLongAndExceedsTheMaximumAllowedLength123!");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_PASSWORD_LENGTH, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - No Uppercase Letter")
    void testValidateEmailAndPassword_NoUppercaseLetter() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "valid@password1");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_PASSWORD_UPPERCASE, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - No Lowercase Letter")
    void testValidateEmailAndPasswordNoLowercaseLetter() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "VALID@PASSWORD1");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_PASSWORD_LOWERCASE, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - No Digit")
    void testValidateEmailAndPassword_NoDigit() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "Valid@Password");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_PASSWORD_NUMBER, result.getLeft()[0].message());
    }

    @Test
    @DisplayName("Validate Email and Password - No Special Character")
    void testValidateEmailAndPassword_NoSpecialCharacter() {
        Either<ErrorDto[], Boolean> result = userValidation.validateEmailAndPassword(validEmail, "ValidPassword1");
        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].code());
        assertEquals(MessageConstants.ERROR_PASSWORD_SPECIAL_CHARACTER, result.getLeft()[0].message());
    }
}
