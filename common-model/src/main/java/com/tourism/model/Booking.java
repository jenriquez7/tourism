package com.tourism.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Bookings", schema = "public")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable=false, updatable=false)
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

}
