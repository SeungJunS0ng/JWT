package com.jwtauth.repository;

import com.jwtauth.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.attemptTime > :since")
    List<LoginAttempt> findRecentAttemptsByIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT la FROM LoginAttempt la WHERE la.username = :username ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findByUsernameOrderByAttemptTimeDesc(@Param("username") String username);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.attemptTime > :since")
    long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username AND la.success = false AND la.attemptTime > :since")
    long countFailedAttemptsByUsernameSince(@Param("username") String username, @Param("since") LocalDateTime since);

    @Query("SELECT la FROM LoginAttempt la WHERE la.success = false AND la.attemptTime BETWEEN :start AND :end")
    List<LoginAttempt> findFailedAttemptsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
