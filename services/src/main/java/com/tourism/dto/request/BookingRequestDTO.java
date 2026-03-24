package com.tourism.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.util.DigestUtils;

import jakarta.validation.constraints.NotNull;

public record BookingRequestDTO(@NotNull LocalDate checkIn, @NotNull LocalDate checkOut, @NotNull UUID lodgingId, @NotNull Integer adults,
                                @NotNull Integer children, @NotNull Integer babies) {

   public String generateIdempotencyKey(UUID touristId) {
      String rawData = touristId.toString() + lodgingId.toString() + checkIn.toString() + checkOut.toString() + adults + children + babies;
      return DigestUtils.md5DigestAsHex(rawData.getBytes());
   }

}
