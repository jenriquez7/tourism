package com.tourism.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BookingUpdateRequestDTO (@NotNull UUID bookingId,
                                       @NotNull LocalDate checkIn,
                                       @NotNull LocalDate checkOut) {}
