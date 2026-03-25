package com.tourism.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications", schema = "public")
public class Notification {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   @Column(insertable = false, updatable = false)
   private UUID id;

   @NonNull
   @NotNull
   @Column(name = "receiver")
   private User receiver;

   @NonNull
   @NotNull
   @Column(name = "message")
   private String message;

   @NonNull
   @NotNull
   @Column(name = "type")
   @Enumerated(EnumType.STRING)
   private MessageType type;

   @Column(name = "sent")
   private Boolean sent = false;

   @CreationTimestamp
   @Column(name = "created_date", nullable = false)
   private Instant createdDate;

}
