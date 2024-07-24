package com.tourism.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Touristic_places", schema = "public")
public class TouristicPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable=false, updatable=false)
    private UUID id;

    @NonNull
    @NotNull
    @Column(nullable = false, length = 70, unique = true)
    private String name;

    @NonNull
    @NotNull
    @Column(nullable = false, length = 2000)
    private String description;

    @NonNull
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Region region;

    @OneToMany(mappedBy = "touristicPlace", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<TouristicPlaceCategory> categories;

    @NonNull
    @NotNull
    @Column(name = "user_creator")
    private User user;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private Instant updatedDate;

    @NonNull
    @NotNull
    @Column(nullable = false)
    private Boolean enabled;

    // TODO: images list

}
