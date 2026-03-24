package com.tourism.service.impl.queues;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.dto.request.BookingMessage;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.service.BookingSendingQueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("bookingQueueServiceKafkaImpl")
@Primary
@Slf4j
@RequiredArgsConstructor
public class BookingSendingQueueServiceKafkaImpl implements BookingSendingQueueService {

   @Value("${spring.kafka.topic.booking}")
   private String bookingTopic;

   private final KafkaTemplate<String, String> kafkaTemplate;

   private final ObjectMapper objectMapper;

   @Override
   public void sendMessage(BookingRequestDTO bookingDto, UUID touristId, String idempotencyKey) {
      CompletableFuture.runAsync(() -> {
         try {
            BookingMessage message = new BookingMessage(bookingDto, touristId, idempotencyKey);
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

}
