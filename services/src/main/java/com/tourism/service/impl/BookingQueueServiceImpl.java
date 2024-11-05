package com.tourism.service.impl;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.dto.request.BookingMessage;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.service.BookingService;
import com.tourism.util.MessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class BookingQueueServiceImpl {

    @Value("${aws.sqs.queue.url}")
    private String queueUrl;

    private final AmazonSQS sqsClient;
    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BookingQueueServiceImpl(AmazonSQS sqsClient, BookingService bookingService, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(BookingRequestDTO bookingDto, UUID touristId) {
        CompletableFuture<Void> messageFuture = CompletableFuture.runAsync(() -> {
            BookingMessage message = new BookingMessage(bookingDto, touristId);
            try {
                String messageBody = objectMapper.writeValueAsString(message);
                SendMessageRequest sendMessageRequest = new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody(messageBody);
                sqsClient.sendMessage(sendMessageRequest);
            } catch (JsonProcessingException e) {
                log.error(MessageConstants.ERROR_FORMATTING_JSON);
            }
            log.info(MessageConstants.MESSAGE_SENT_TO_OBSERVERS, touristId, bookingDto.lodging().getLodgingOwner().getId());
        });

        messageFuture.whenComplete((unused, throwable) -> {
        if (throwable != null) {
            log.error(MessageConstants.ERROR_SENDING_MESSAGE, throwable.getMessage());
        } else {
            log.info(MessageConstants.MESSAGE_SUCCESSFULLY);
        }
    });
    }

    @SqsListener("${aws.sqs.queue.url}")
    public void receiveMessage(String messageBody) {
        try {
            BookingMessage message = objectMapper.readValue(messageBody, BookingMessage.class);
            bookingService.processBooking(message);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing booking message", e);
        }
    }
}
