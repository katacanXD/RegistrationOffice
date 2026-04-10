package ru.katacan.registrationoffice.repository;

import ru.katacan.registrationoffice.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {

    Optional<JwtToken> findByTokenHash(String tokenHash);

    List<JwtToken> findAllByUserIdAndRevokedAtIsNullAndExpiryDateAfter(Long userId, Instant now);

    long deleteByExpiryDateBefore(Instant now);

    @Modifying
    @Query("update JwtToken t set t.revokedAt = :now where t.user.id = :userId and t.revokedAt is null and t.expiryDate > :now")
    int revokeAllActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying
    @Query("update JwtToken t set t.revokedAt = :now where t.revokedAt is null and t.expiryDate > :now")
    int revokeAllActive(@Param("now") Instant now);
}

