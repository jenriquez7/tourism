package com.tourism.service.impl;

import org.springframework.stereotype.Service;

import com.tourism.model.Notification;
import com.tourism.repository.NotificationRepository;
import com.tourism.service.NotificationService;
import com.tourism.util.MessageConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

   private final NotificationRepository repository;

   @Override
   public void createNotification(Notification notification) {
      try {
         repository.save(notification);
      } catch (Exception e) {
         log.error(MessageConstants.ERROR_CREATE_NOTIFICATION);
      }
   }

}
