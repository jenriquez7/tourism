package com.tourism.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

import java.util.UUID;

public record LodgingRequestDTO(@NonNull @NotNull String name,
                                @NonNull @NotNull String description,
                                @NonNull @NotNull String information,
                                @NonNull @NotNull String phone,
                                @Min(1) @Max(5) @NonNull @NotNull Integer stars,
                                @NonNull @NotNull Integer capacity,
                                @NonNull @NotNull Double nightPrice,
                                @NonNull @NotNull UUID touristicPlaceId) {
}
