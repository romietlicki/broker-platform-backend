package com.brokerplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "broker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Broker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true, nullable = false, length = 36)
    private String uuid;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "cpf", unique = true, length = 14)
    private String cpf;

    @Column(name = "susep_code", unique = true, length = 20)
    private String susepCode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address_street", length = 200)
    private String addressStreet;

    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Column(name = "address_state", columnDefinition = "CHAR(2)")
    private String addressState;

    @Column(name = "address_zip", length = 10)
    private String addressZip;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "is_locked")
    @Builder.Default
    private boolean locked = false;

    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "broker_role",
            joinColumns = @JoinColumn(name = "broker_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "broker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BrokerSettings settings;

    @PrePersist
    private void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.locked = true;
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.locked = false;
    }

    public enum Status {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
