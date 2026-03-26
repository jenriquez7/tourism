package com.tourism.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "touristic_places", schema = "public")
public class TouristicPlace {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   @Column(insertable = false, updatable = false)
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

   @OneToMany(mappedBy = "touristicPlace", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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

   public TouristicPlace(@NonNull String name, @NonNull String description, @NonNull Region region, List<TouristicPlaceCategory> categories,
         @NonNull User user, @NonNull Boolean enabled) {
      this.name = name;
      this.description = description;
      this.region = region;
      this.categories = categories;
      this.user = user;
      this.enabled = enabled;
   }

}
