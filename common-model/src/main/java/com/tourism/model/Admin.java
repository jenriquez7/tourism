package com.tourism.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "admins", schema = "public")
public class Admin extends User {

   public Admin(String email, String password, Boolean enabled) {
      super(UUID.randomUUID(), email, Role.ADMIN);
      this.password = password;
      this.enabled = enabled;
   }

}
