package com.tourism.service.impl.queues;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.dto.request.BookingMessage;
import com.tourism.service.BookingReceiveQueueService;
import com.tourism.service.BookingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("bookingReceiveQueueServiceKafkaImpl")
@Slf4j
@RequiredArgsConstructor
public class BookingReceiveQueueServiceKafkaImpl implements BookingReceiveQueueService {

   private final ObjectMapper objectMapper;

   private final BookingService bookingService;

   @Value("${spring.kafka.topic.booking}")
   private String bookingTopic;

   @KafkaListener(topics = "${spring.kafka.topic.booking}", groupId = "${spring.kafka.consumer.group-id}")
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
