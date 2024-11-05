package com.tourism.service;

import com.tourism.dto.request.BookingMessage;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Booking;
import com.tourism.model.BookingState;
import com.tourism.model.LodgingOwner;
import com.tourism.model.Tourist;
import com.tourism.observer.BookingObserver;
import io.vavr.control.Either;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BookingService {

    Either<ErrorDto[], String> create(BookingRequestDTO bookingDto, UUID touristId);
    void processBooking(BookingMessage bookingMessage);
    Either<ErrorDto[], String> update(BookingUpdateRequestDTO bookingDto, UUID touristId);
    Either<ErrorDto[], Page<BookingResponseDTO>> findAll(PageableRequest paging);
    Either<ErrorDto[], Booking> delete(UUID id);
    Either<ErrorDto[], BookingResponseDTO> getById(UUID id);
    Either<ErrorDto[], BookingResponseDTO> changeState(UUID bookingId, BookingState state, UUID userId);
    void updateToExpiredBookings();
    void notifyObservers(String lodgingName, UUID bookingId, Tourist tourist, LodgingOwner owner, BookingState state);
    void addObserver(BookingObserver observer);
    void removeObserver(BookingObserver observer);
}
