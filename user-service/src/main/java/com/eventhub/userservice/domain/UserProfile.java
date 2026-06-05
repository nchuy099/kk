package com.eventhub.userservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {
    @Id
    private String id;

    @Column(nullable = false)
    private String username;

    @Column
    private String email;

    @Column
    private String fullName;

    @Column
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public static UserProfile create(String id, String username, String email, String fullName) {
        var user = new UserProfile();
        user.id = id;
        user.username = username;
        user.email = email;
        user.fullName = fullName;
        user.status = UserStatus.ACTIVE;
        user.createdAt = Instant.now();
        user.updatedAt = Instant.now();
        return user;
    }

    public void syncIdentity(String username, String email) {
        this.username = username;
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

}
