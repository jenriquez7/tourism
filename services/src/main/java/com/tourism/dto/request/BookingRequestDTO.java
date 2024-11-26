package com.tourism.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BookingRequestDTO (@NotNull LocalDate checkIn,
                                 @NotNull LocalDate checkOut,
                                 @NotNull UUID lodgingId,
                                 @NotNull Integer adults,
                                 @NotNull Integer children,
                                 @NotNull Integer babies) {
}
