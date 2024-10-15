package com.tourism.dto.response;

import com.tourism.model.BookingState;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponseDTO(UUID id, String lodgingName, String firstName, String lastName, LocalDate checkIn,
                                 LocalDate checkOut, Double totalPrice, String lodgingPhone, String lodgingInformation,
                                 BookingState state) { }
