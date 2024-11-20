package com.tourism.service;

import com.tourism.dto.request.BookingRequestDTO;

import java.util.UUID;

public interface BookingQueueService {
    void sendMessage(BookingRequestDTO bookingDto, UUID touristId);
    void receiveMessage(String messageBody);
}
