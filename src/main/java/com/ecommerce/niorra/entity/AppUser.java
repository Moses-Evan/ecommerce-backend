package com.ecommerce.niorra.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = { "provider", "provider_user_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    private String profilePicture;

    private Boolean emailVerified;

    private String locale;

    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
