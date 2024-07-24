package com.tourism.test.util;

import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Role;
import com.tourism.model.*;
import com.tourism.repository.BookingDateRepository;
import com.tourism.util.MessageConstants;
import com.tourism.util.validations.BookingValidation;
import com.tourism.util.validations.DateValidation;
import io.vavr.control.Either;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookingValidationTests {

    @Mock
    private BookingDateRepository dateRepository;

    @Mock
    private DateValidation dateValidation;

    @InjectMocks
    private BookingValidation bookingValidation;

    private BookingRequestDTO bookingDto;
    private Tourist tourist;
    private Lodging lodging;
    private LodgingOwner owner;
    private Booking booking;

    @BeforeEach
    void setUp() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        tourist = new Tourist("tverano@email.com", "validPassword123", "Turista", "Verano", Role.TOURIST, TouristType.STANDARD, true);
        owner = new LodgingOwner("owner@email.com", "validPassword123", "Owner", "Hotel", Role.LODGING_OWNER, true);
        owner.setId(UUID.randomUUID());
        tourist.setId(UUID.randomUUID());
        lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Calle falsa 123", "+59899123456", 5, 25.0, 4, new TouristicPlace(), owner, true);
        lodging.setId(UUID.randomUUID());
        bookingDto = new BookingRequestDTO(checkIn, checkOut, lodging, 2,1,1);
        booking = new Booking(checkIn, checkOut, 100.0, lodging, tourist, BookingState.CREATED, 2, 1, 1, false);
        booking.setId(UUID.randomUUID());

    }

    @Test
    @DisplayName("Validate Booking - Valid Booking")
    void testValidBooking() {
        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, lodging);

        assertTrue(result.isRight());
        assertTrue(result.get());
    }

    @Test
    @DisplayName("Validate Booking - null tourist")
    void testNullTourist() {
        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, null, lodging);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_TOURIST_NOT_FOUND, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Validate Booking - null lodging")
    void testNullLodging() {
        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, null);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.NOT_FOUND, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_NOT_FOUND, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Validate Booking - CheckOut Before CheckIn")
    void testCheckOutBeforeCheckIn() {
        when(dateValidation.checkOutBeforeCheckIn(any(), any())).thenReturn(true);

        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, lodging);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_CHECK_IN_AFTER_CHECKOUT, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Validate Booking - CheckIn Before Today")
    void testCheckInBeforeToday() {
        when(dateValidation.checkInBeforeToday(any())).thenReturn(true);

        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, lodging);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_CHECK_IN_IN_THE_PAST, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Validate Booking - Invalid Lodging Capacity")
    void testInvalidLodgingCapacity() {
        List<LocalDate> mockDates = Arrays.asList(
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 2),
                LocalDate.of(2024, 7, 3)
        );
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);

        BookingDate mockBookingDate = new BookingDate();
        Booking mockBooking = new Booking();
        mockBooking.setAdults(2);
        mockBooking.setChildren(1);
        mockBooking.setBabies(1);
        mockBookingDate.setBooking(mockBooking);
        List<BookingDate> mockBookingDates = Collections.singletonList(mockBookingDate);
        when(dateRepository.findBookingDatesByLodgingAndStateAndDateBetweenCheckInAndCheckOutOrderByCheckInAsc(
                any(), any(), eq(BookingState.ACCEPTED))).thenReturn(mockBookingDates);
        lodging.setCapacity(5);

        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, lodging);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_ENOUGH_CAPACITY, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Validate Booking - Booking Without Adult")
    void testBookingWithoutAdult() {
        bookingDto.setAdults(0);

        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, lodging);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_BOOKING_WITHOUT_ADULT, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Validate Booking - Multiple Errors")
    void testMultipleErrors() {
        when(dateValidation.checkOutBeforeCheckIn(any(), any())).thenReturn(true);
        when(dateValidation.checkInBeforeToday(any())).thenReturn(true);
        bookingDto.setAdults(0);

        Either<ErrorDto[], Boolean> result = bookingValidation.validateBooking(bookingDto, tourist, lodging);

        assertTrue(result.isLeft());
        assertEquals(3, result.getLeft().length);
    }

    @Test
    @DisplayName("Invalid Lodging Capacity - Capacity Exceeded")
    void testInvalidLodgingCapacityVsBookingsCapacityExceeded() {
        lodging.setCapacity(5);

        List<LocalDate> mockDates = Arrays.asList(
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );
        when(dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut())).thenReturn(mockDates);

        BookingDate mockBookingDate = new BookingDate();
        Booking mockBooking = new Booking();
        mockBooking.setAdults(3);
        mockBooking.setChildren(1);
        mockBooking.setBabies(0);
        mockBookingDate.setBooking(mockBooking);
        List<BookingDate> mockBookingDates = Collections.singletonList(mockBookingDate);

        when(dateRepository.findBookingDatesByLodgingAndStateAndDateBetweenCheckInAndCheckOutOrderByCheckInAsc(
                eq(lodging), any(), eq(BookingState.ACCEPTED))).thenReturn(mockBookingDates);

        boolean result = bookingValidation.invalidLodgingCapacityVsBookings(2, 1, 0,
                bookingDto.getCheckIn(), bookingDto.getCheckOut(), lodging);

        assertTrue(result);
    }

    @Test
    @DisplayName("Invalid Lodging Capacity - Capacity Not Exceeded")
    void testInvalidLodgingCapacityVsBookingsCapacityNotExceeded() {
        lodging.setCapacity(10);

        List<LocalDate> mockDates = Arrays.asList(
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2024, 7, 2)
        );
        when(dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut())).thenReturn(mockDates);

        BookingDate mockBookingDate = new BookingDate();
        Booking mockBooking = new Booking();
        mockBooking.setAdults(3);
        mockBooking.setChildren(1);
        mockBooking.setBabies(0);
        mockBookingDate.setBooking(mockBooking);
        List<BookingDate> mockBookingDates = Collections.singletonList(mockBookingDate);

        when(dateRepository.findBookingDatesByLodgingAndStateAndDateBetweenCheckInAndCheckOutOrderByCheckInAsc(
                eq(lodging), any(), eq(BookingState.ACCEPTED))).thenReturn(mockBookingDates);

        boolean result = bookingValidation.invalidLodgingCapacityVsBookings(2, 1, 0,
                bookingDto.getCheckIn(), bookingDto.getCheckOut(), lodging);

        assertFalse(result);
    }

    @Test
    @DisplayName("Valid Change State - Created To Rejected Valid Owner")
    void testValidChangeStateCreatedToRejectedValidOwner() {
        booking.setState(BookingState.CREATED);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.REJECTED, owner.getId());

        assertTrue(result.isRight());
        assertTrue(result.get());
    }

    @Test
    @DisplayName("Valid Change State - Created To Rejected Invalid Owner")
    void testValidChangeStateCreatedToRejectedInvalidOwner() {
        UUID differentUserId = UUID.randomUUID();
        booking.setState(BookingState.CREATED);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.REJECTED, differentUserId);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_USER_LODGING_OWNER, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Valid Change State - Created To Invalid State")
    void testValidChangeStateCreatedToInvalidState() {
        booking.setState(BookingState.CREATED);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.ACCEPTED, owner.getId());

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_BOOKING_CHANGE_STATE, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Valid Change State - Pending To Accepted Valid Tourist")
    void testValidChangeStatePendingToAcceptedValidTourist() {
        booking.setState(BookingState.PENDING);
        booking.setAdults(1);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.ACCEPTED, tourist.getId());

        assertTrue(result.isRight());
        assertTrue(result.get());
    }

    @Test
    @DisplayName("Valid Change State - Pending To Accepted Invalid Tourist")
    void testValidChangeStatePendingToAcceptedInvalidTourist() {
        UUID differentUserId = UUID.randomUUID();
        booking.setState(BookingState.PENDING);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.ACCEPTED, differentUserId);

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_USER_TOURIST, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Valid Change State - Pending To Invalid State")
    void testValidChangeStatePendingToInvalidState() {
        booking.setState(BookingState.PENDING);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.REJECTED, tourist.getId());

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_BOOKING_CHANGE_STATE, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Valid Change State - Other State")
    void testValidChangeStateOtherState() {
        UUID userId = UUID.randomUUID();
        booking.setState(BookingState.ACCEPTED);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.REJECTED, userId);

        assertTrue(result.isRight());
        assertTrue(result.get());
    }

    @Test
    @DisplayName("Valid Change State - Invalid Capacity")
    void testValidChangeStateInvalidCapacity() {
        List<LocalDate> mockDates = Arrays.asList(
                bookingDto.getCheckIn(),
                bookingDto.getCheckIn().plusDays(1)
        );
        booking.setState(BookingState.CREATED);
        booking.setAdults(30);
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.PENDING, owner.getId());

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_ENOUGH_CAPACITY, result.getLeft()[0].getMessage());
    }

    @Test
    @DisplayName("Valid Change State - No Adults")
    void testValidChangeState_NoAdults() {
        booking.setState(BookingState.CREATED);
        booking.setAdults(0);

        Either<ErrorDto[], Boolean> result = bookingValidation.validChangeState(booking, BookingState.PENDING, owner.getId());

        assertTrue(result.isLeft());
        assertEquals(HttpStatus.BAD_REQUEST, result.getLeft()[0].getCode());
        assertEquals(MessageConstants.ERROR_BOOKING_WITHOUT_ADULT, result.getLeft()[0].getMessage());
    }
}
