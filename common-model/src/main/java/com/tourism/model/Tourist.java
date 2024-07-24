package com.tourism.model;

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
@Table(name = "Tourists", schema = "public")
public class Tourist extends User {

    @Column(name = "first_name", nullable = false, length = 30)
    @NotNull
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotNull
    private String lastName;

    @Column(name = "tourist_type", nullable = false, length = 50)
    @NotNull
    private TouristType type;

    public Tourist(String email, String password, String firstName, String lastName, Role role,
                   TouristType type, Boolean enabled) {
        super();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.type = type;
        this.enabled = enabled;
    }

}
