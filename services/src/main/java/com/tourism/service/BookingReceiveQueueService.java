package com.tourism.service;

public interface BookingReceiveQueueService {
    void receiveMessage(String messageBody);
}
