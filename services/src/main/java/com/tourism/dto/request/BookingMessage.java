package com.tourism.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

import java.util.UUID;

public record BookingMessage(@NonNull @NotNull BookingRequestDTO bookingRequest,
                             @NonNull @NotNull UUID touristId) {}
