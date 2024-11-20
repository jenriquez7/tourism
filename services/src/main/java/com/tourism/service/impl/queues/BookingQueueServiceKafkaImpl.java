package com.tourism.service.impl.queues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.dto.request.BookingMessage;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.service.BookingQueueService;
import com.tourism.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@Primary
public class BookingQueueServiceKafkaImpl implements BookingQueueService {

    @Value("${kafka.topic.booking}")
    private String bookingTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final BookingService bookingService;

    @Autowired
    public BookingQueueServiceKafkaImpl(KafkaTemplate<String, String> kafkaTemplate, BookingService bookingService, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendMessage(BookingRequestDTO bookingDto, UUID touristId) {
        CompletableFuture.runAsync(() -> {
            try {
                BookingMessage message = new BookingMessage(bookingDto, touristId);
                String messageBody = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(bookingTopic, messageBody);
                log.info("Message sent to Kafka topic [{}]: {}", bookingTopic, messageBody);
            } catch (JsonProcessingException e) {
                log.error("Error serializing booking message", e);
            }
        }).exceptionally(throwable -> {
            log.error("Error sending message to Kafka topic [{}]: {}", bookingTopic, throwable.getMessage());
            return null;
        });
    }

    @KafkaListener(topics = "${kafka.topic.booking}", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    public void receiveMessage(String messageBody) {
        try {
            BookingMessage message = objectMapper.readValue(messageBody, BookingMessage.class);
            bookingService.processBooking(message);
            log.info("Message processed from Kafka topic [{}]: {}", bookingTopic, message);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing booking message", e);
        }
    }
}
