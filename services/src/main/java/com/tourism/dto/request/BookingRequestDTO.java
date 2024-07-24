package com.tourism.dto.request;

import com.tourism.model.Lodging;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Create a new lodging owner DTO")
public class BookingRequestDTO {

    @Schema(description = "when the booking starts")
    @NotNull
    private LocalDate checkIn;

    @Schema(description = "when the booking starts")
    @NotNull
    private LocalDate checkOut;

    @Schema(description = "Lodging booking")
    @NotNull
    private Lodging lodging;

    @Schema(description = "count of adults. One at least")
    @NotNull
    private Integer adults;

    @Schema(description = "count of children")
    @NotNull
    private Integer children;

    @Schema(description = "count of babies")
    @NotNull
    private Integer babies;
}
