package com.tourism.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

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
    @Column(insertable=false, updatable=false)
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
