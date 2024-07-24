package com.tourism.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "logins", schema = "public")
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable=false, updatable=false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, name = "login_time")
    private Timestamp loginTime;

    @Column(nullable = false)
    private Boolean successful;

    @PrePersist
    protected void onCreate() {
        this.loginTime = new Timestamp(System.currentTimeMillis());
    }

    public Login(String email, boolean successful) {
        this.email = email;
        this.successful = successful;
    }
}
