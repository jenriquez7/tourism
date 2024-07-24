package com.tourism.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "Admins", schema = "public")
public class Admin extends User {

    public Admin(String email, String password, Role role, Boolean enabled) {
        super();
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }
}
