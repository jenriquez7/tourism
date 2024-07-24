package com.tourism.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Lodgings", schema = "public")
public class Lodging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable=false, updatable=false)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    @NonNull
    @NotNull
    private String name;

    @Column(nullable = false)
    @NonNull
    @NotNull
    private String description;

    @Column(nullable = false)
    @NonNull
    @NotNull
    private String information;

    @Column(nullable = false, length = 30)
    @NonNull
    @NotNull
    private String phone;

    @Column(nullable = false)
    @NonNull
    @NotNull
    private Integer capacity;

    @Column(name = "night_price", nullable = false)
    @NonNull
    @NotNull
    private Double nightPrice;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    @NonNull
    @NotNull
    private Integer stars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "touristic_place_id", nullable = false)
    @ToString.Exclude
    @NonNull
    @NotNull
    private TouristicPlace touristicPlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lodging_owner_id", nullable = false)
    @ToString.Exclude
    @NonNull
    @NotNull
    private LodgingOwner lodgingOwner;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private Instant updatedDate;

    @Column(nullable = false)
    @NonNull
    @NotNull
    private Boolean enabled;

    // TODO: images list


    public void updateLodgingFromDTO(Lodging lodging, TouristicPlace place) {
        this.setName(lodging.getName());
        this.setDescription(lodging.getDescription());
        this.setInformation(lodging.getInformation());
        this.setPhone(lodging.getPhone());
        this.setCapacity(lodging.getCapacity());
        this.setNightPrice(lodging.getNightPrice());
        this.setStars(lodging.getStars());
        this.setTouristicPlace(place);
    }
}
