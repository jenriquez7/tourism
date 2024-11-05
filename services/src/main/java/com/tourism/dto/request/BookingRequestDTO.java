package com.tourism.dto.request;

import com.tourism.model.Lodging;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequestDTO (@NotNull LocalDate checkIn,
                                 @NotNull LocalDate checkOut,
                                 @NotNull Lodging lodging,
                                 @NotNull Integer adults,
                                 @NotNull Integer children,
                                 @NotNull Integer babies) {
}
