package com.tourism.service;

import com.tourism.dto.request.BookingRequestDTO;

import java.util.UUID;

public interface BookingSendingQueueService {
    void sendMessage(BookingRequestDTO bookingDto, UUID touristId);
}
