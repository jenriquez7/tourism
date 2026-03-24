package com.tourism.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "Admins", schema = "public")
public class Admin extends User {

    public Admin(String email, String password, Role role, Boolean enabled) {
        super(UUID.randomUUID(), email, role);
        this.password = password;
        this.enabled = enabled;
    }
}
