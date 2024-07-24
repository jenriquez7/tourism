package com.tourism.service.impl;

import com.tourism.model.Notification;
import com.tourism.repository.NotificationRepository;
import com.tourism.service.NotificationService;
import com.tourism.util.MessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }


    @Override
    public void createNotification(Notification notification) {
        try {
            repository.save(notification);
        } catch (Exception e) {
            log.error(MessageConstants.ERROR_CREATE_NOTIFICATION);
        }
    }
}
