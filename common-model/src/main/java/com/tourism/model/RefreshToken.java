package com.tourism.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Entity
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, columnDefinition = "text")
    @NonNull
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NonNull
    private User user;
}
