package com.jwtauth.repository;

import com.jwtauth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    List<RefreshToken> findByUsernameAndRevokedFalse(String username);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.username = :username AND rt.revoked = false AND rt.expiryDate > :now")
    long countActiveTokensByUsername(@Param("username") String username, @Param("now") LocalDateTime now);

    default long countActiveTokensByUsername(String username) {
        return countActiveTokensByUsername(username, LocalDateTime.now());
    }

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.username = :username AND rt.revoked = false AND rt.expiryDate > :now ORDER BY rt.createdAt ASC")
    List<RefreshToken> findActiveTokensByUsernameOrderByCreatedAt(@Param("username") String username, @Param("now") LocalDateTime now);

    default List<RefreshToken> findActiveTokensByUsernameOrderByCreatedAt(String username) {
        return findActiveTokensByUsernameOrderByCreatedAt(username, LocalDateTime.now());
    }

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.username = :username AND rt.revoked = false")
    int revokeAllUserTokens(@Param("username") String username);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :threshold AND rt.revoked = true")
    int deleteExpiredTokens(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.expiryDate < :now AND rt.revoked = false")
    int revokeExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.username = :username ORDER BY rt.lastUsedAt DESC, rt.createdAt DESC")
    List<RefreshToken> findByUsernameOrderByLastUsedAtDesc(@Param("username") String username);
}
