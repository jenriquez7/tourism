package com.tourism.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class TouristicPlaceCategoryId implements Serializable {

    private UUID touristicPlaceId;
    private Integer categoryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TouristicPlaceCategoryId tpc = (TouristicPlaceCategoryId) o;
        return Objects.equals(touristicPlaceId, tpc.touristicPlaceId) &&
                Objects.equals(categoryId, tpc.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(touristicPlaceId, categoryId);
    }
}
