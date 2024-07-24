package com.tourism.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "touristic_place_categories", schema = "public")
public class TouristicPlaceCategory {

    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private TouristicPlaceCategoryId id;

    @ManyToOne
    @MapsId("touristicPlaceId")
    @JoinColumn(name = "touristic_place_id")
    @JsonIgnore
    @NonNull
    @NotNull
    private TouristicPlace touristicPlace;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    @NonNull
    @NotNull
    private Category category;

}
