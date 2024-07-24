package com.tourism.dto.response;

import com.tourism.model.Booking;
import com.tourism.model.BookingState;
import com.tourism.model.TouristicPlace;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {

    private UUID bookingId;
    private String lodgingName;
    private String firstName;
    private String lastName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Double totalPrice;
    private String lodgingPhone;
    private String lodgingInformation;
    private BookingState state;

    public static BookingResponseDTO bookingToResponseDTO(Booking booking) {
        return (new BookingResponseDTO(
                booking.getId(),
                booking.getLodging().getName(),
                booking.getTourist().getFirstName(),
                booking.getTourist().getLastName(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getTotalPrice(),
                booking.getLodging().getPhone(),
                booking.getLodging().getInformation(),
                booking.getState()
        ));
    }
}
