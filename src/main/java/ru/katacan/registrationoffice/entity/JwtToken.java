package ru.katacan.registrationoffice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "t_refresh_tokens")
@Setter
@Getter
@NoArgsConstructor
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_token_id")
    private Long id;

    @Column(name = "f_token_hash", nullable = false, unique = true, length = 512)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "f_user_id", nullable = false)
    private User user;

    @Column(name = "f_expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "f_created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "f_revoked_at")
    private Instant revokedAt;

    @Column(name = "f_replaced_by_token_hash", length = 512)
    private String replacedByTokenHash;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
