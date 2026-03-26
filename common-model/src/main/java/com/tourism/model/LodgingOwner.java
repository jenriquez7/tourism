package com.tourism.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "owners", schema = "public")
public class LodgingOwner extends User {

   @Column(name = "first_name", nullable = false, length = 30)
   @NotNull
   private String firstName;

   @Column(name = "last_name", nullable = false, length = 50)
   @NotNull
   private String lastName;

   public LodgingOwner(String email, String password, String firstName, String lastName, Boolean enabled) {
      super(UUID.randomUUID(), email, Role.LODGING_OWNER);
      this.password = password;
      this.firstName = firstName;
      this.lastName = lastName;
      this.enabled = enabled;
   }

}
