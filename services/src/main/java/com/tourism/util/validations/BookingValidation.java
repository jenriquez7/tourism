package com.tourism.util.validations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.vavr.control.Either;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Booking;
import com.tourism.model.BookingDate;
import com.tourism.model.BookingState;
import com.tourism.model.Lodging;
import com.tourism.model.Tourist;
import com.tourism.repository.BookingDateRepository;
import com.tourism.util.MessageConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingValidation {

   private final BookingDateRepository dateRepository;

   private final DateValidation dateValidation;

   public Either<ErrorDto[], Boolean> validateBooking(BookingRequestDTO bookingDTO, Tourist tourist, Lodging lodging)
         throws IllegalArgumentException {
      List<ErrorDto> errors = new ArrayList<>();
      if (tourist == null) {
         errors.add(ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.ERROR_TOURIST_NOT_FOUND));
      } else if (lodging == null) {
         errors.add(ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.ERROR_LODGING_NOT_FOUND));
      } else {
         if (dateValidation.checkOutBeforeCheckIn(bookingDTO.checkIn(), bookingDTO.checkOut())) {
            errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CHECK_IN_AFTER_CHECKOUT));
         }
         if (dateValidation.checkInBeforeToday(bookingDTO.checkIn())) {
            errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CHECK_IN_IN_THE_PAST));
         }
         if (bookingDTO.adults() <= 0) {
            errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_BOOKING_WITHOUT_ADULT));
         }
      }
      return getErrorResponse(errors);
   }

   public boolean validLodgingCapacityVsBookings(int adults, int children, int babies, LocalDate checkIn, LocalDate checkOut, Lodging lodging)
         throws IllegalArgumentException {
      int personsInBooking = adults + children + babies;
      List<LocalDate> bookingDates = dateValidation.datesBetweenDates(checkIn, checkOut);
      for (LocalDate date : bookingDates) {
         List<BookingDate> bookings = dateRepository.findBookingDatesByLodgingAndStateAndDateBetweenCheckInAndCheckOutOrderByCheckInAsc(lodging, date,
               BookingState.ACCEPTED);
         int totalPersons = bookings
               .stream()
               .mapToInt(b -> b.getBooking().getAdults() + b.getBooking().getChildren() + b.getBooking().getBabies())
               .sum();
         if (lodging.getCapacity() - totalPersons - personsInBooking < 0) {
            return false;
         }
      }
      return true;
   }

   public Either<ErrorDto[], Boolean> validChangeState(Booking booking, BookingState newState, UUID userId) throws IllegalArgumentException {
      List<ErrorDto> errors = new ArrayList<>();
      switch (booking.getState()) {
         case CREATED -> {
            if (!booking.getLodging().getLodgingOwner().getId().equals(userId)) {
               errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_USER_LODGING_OWNER));
            }
            if (!Arrays.asList(BookingState.PENDING_PAYMENT, BookingState.REJECTED).contains(newState)) {
               errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_BOOKING_CHANGE_STATE));
            }
         }
         case PENDING_PAYMENT -> {
            if (!booking.getTourist().getId().equals(userId)) {
               errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_USER_TOURIST));
            }
            if (!newState.equals(BookingState.ACCEPTED)) {
               errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_BOOKING_CHANGE_STATE));
            }
         }
         default -> {
            return Either.right(true);
         }
      }
      if (booking.getAdults() <= 0) {
         errors.add(ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_BOOKING_WITHOUT_ADULT));
      }
      return getErrorResponse(errors);
   }

   private static Either<ErrorDto[], Boolean> getErrorResponse(List<ErrorDto> errors) {
      if (errors.isEmpty()) {
         return Either.right(true);
      } else {
         return Either.left(errors.toArray(new ErrorDto[0]));
      }
   }

}
