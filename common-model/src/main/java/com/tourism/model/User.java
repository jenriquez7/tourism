package com.tourism.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@Entity
@Table(name = "users", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@ToString()
public class User implements Serializable {

   @Id
   @GeneratedValue(strategy = GenerationType.UUID)
   @Column(updatable = false)
   protected UUID id;

   @Column(nullable = false, length = 100, unique = true)
   @NonNull
   protected String email;

   @Column(nullable = false)
   @NonNull
   protected String password;

   @Column(nullable = false)
   @Enumerated(EnumType.STRING)
   @NonNull
   protected Role role;

   @Column(nullable = false)
   @NonNull
   protected Boolean enabled;

   public User(UUID id, @NonNull String email, @NonNull Role role) {
      this.id = id;
      this.email = email;
      this.role = role;
   }

}
