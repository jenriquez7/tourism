package com.tourism.service;

import java.util.UUID;

import com.tourism.dto.request.BookingRequestDTO;

public interface BookingSendingQueueService {

   void sendMessage(BookingRequestDTO bookingDto, UUID touristId, String idempotencyKey);

}
