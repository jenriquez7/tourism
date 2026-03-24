package com.tourism.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "Owners", schema = "public")
public class LodgingOwner extends User {

    @Column(name = "first_name", nullable = false, length = 30)
    @NotNull
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotNull
    private String lastName;

    public LodgingOwner(String email, String password, String firstName, String lastName, Role role, Boolean enabled) {
        super(UUID.randomUUID(), email, role);
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
    }

}
