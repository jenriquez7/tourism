package com.tourism.dto.request;

import com.tourism.model.Booking;
import com.tourism.model.Lodging;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Create a new lodging owner DTO")
public class BookingUpdateRequestDTO {

    @Schema(description = "booking id")
    @NotNull
    private UUID bookingId;

    @Schema(description = "when the booking starts")
    @NotNull
    private LocalDate checkIn;

    @Schema(description = "when the booking starts")
    @NotNull
    private LocalDate checkOut;


    public BookingRequestDTO transformRequestDTO(Lodging lodging, Booking booking) {
        BookingRequestDTO bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setLodging(lodging);
        bookingRequestDTO.setAdults(booking.getAdults());
        bookingRequestDTO.setChildren(booking.getChildren());
        bookingRequestDTO.setBabies(booking.getBabies());
        bookingRequestDTO.setCheckIn(this.checkIn);
        bookingRequestDTO.setCheckOut(this.checkOut);
        return bookingRequestDTO;
    }
}
