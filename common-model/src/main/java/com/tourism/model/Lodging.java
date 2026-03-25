package com.tourism.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(name = "Lodgings", schema = "public")
public class Lodging {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   @Column(insertable = false, updatable = false)
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

   @Column(nullable = false)
   @NonNull
   @NotNull
   private Boolean autoAccept;

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
      this.setAutoAccept(lodging.getAutoAccept());
   }

}
