package com.tourism.service;

import java.util.UUID;

import io.vavr.control.Either;

import org.springframework.data.domain.Page;

import com.tourism.dto.request.BookingMessage;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Booking;
import com.tourism.model.BookingState;

public interface BookingService {

   Either<ErrorDto[], String> create(BookingRequestDTO bookingDto, UUID touristId);

   void processBooking(BookingMessage bookingMessage);

   Either<ErrorDto[], String> update(BookingUpdateRequestDTO bookingDto, UUID touristId);

   Either<ErrorDto[], Page<BookingResponseDTO>> findAll(PageableRequest paging);

   Either<ErrorDto[], Booking> delete(UUID id);

   Either<ErrorDto[], BookingResponseDTO> getById(UUID id);

   Either<ErrorDto[], BookingResponseDTO> changeState(UUID bookingId, BookingState state, UUID userId);

   void updateToExpiredBookings();

}
