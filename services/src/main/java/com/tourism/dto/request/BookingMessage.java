package com.tourism.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

public record BookingMessage(@NonNull @NotNull BookingRequestDTO bookingRequest, @NonNull @NotNull UUID touristId,
                             @NonNull @NotNull String idempotencyKey) {

}
