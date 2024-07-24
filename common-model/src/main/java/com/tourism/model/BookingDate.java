package com.tourism.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Booking_dates", schema = "public")
public class BookingDate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable=false, updatable=false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    @NonNull
    @NotNull
    private Booking booking;

    @Column(name = "date", nullable = false)
    @NonNull
    @NotNull
    private LocalDate date;

    @Column(nullable = false)
    @NonNull
    @NotNull
    private Double nightPrice;

}
