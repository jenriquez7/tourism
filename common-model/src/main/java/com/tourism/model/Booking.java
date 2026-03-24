package com.tourism.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Bookings", schema = "public")
public class Booking {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   @Column(insertable = false, updatable = false)
   private UUID id;

   @Column(name = "check_in", nullable = false)
   @NonNull
   @NotNull
   private LocalDate checkIn;

   @Column(name = "check_out", nullable = false)
   @NonNull
   @NotNull
   private LocalDate checkOut;

   @Column(nullable = false)
   @NonNull
   @NotNull
   private Double totalPrice;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "lodging_id", nullable = false)
   @ToString.Exclude
   @NonNull
   @NotNull
   private Lodging lodging;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "tourist_id", nullable = false)
   @ToString.Exclude
   @NonNull
   @NotNull
   private Tourist tourist;

   @Column(nullable = false)
   @Enumerated(EnumType.STRING)
   @NonNull
   @NotNull
   private BookingState state;

   @Column(nullable = false)
   @NonNull
   @NotNull
   private Integer adults;

   @Column(nullable = false)
   @NonNull
   @NotNull
   private Integer children;

   @Column(nullable = false)
   @NonNull
   @NotNull
   private Integer babies;

   @Column(nullable = false)
   @NonNull
   @NotNull
   private Boolean hasPaid;

   @CreationTimestamp
   @Column(name = "created_date", nullable = false)
   private Instant createdDate;

   @UpdateTimestamp
   @Column(name = "updated_date", nullable = false)
   private Instant updatedDate;

   @Column(name = "idempotency_key", nullable = false)
   @NonNull
   @NotNull
   private String idempotencyKey;

}
