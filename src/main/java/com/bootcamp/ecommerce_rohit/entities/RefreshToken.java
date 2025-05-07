package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String refreshToken;

    private String email;

    private Instant createdAt;

    public RefreshToken(String refreshToken, Instant createdAt, String email){
        this.refreshToken = refreshToken;
        this.createdAt = createdAt;
        this.email = email;
    }
}
