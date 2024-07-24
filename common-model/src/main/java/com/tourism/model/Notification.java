package com.tourism.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications", schema = "public")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable=false, updatable=false)
    private UUID id;

    @Nullable
    @Column(name = "sender")
    private User sender;

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
