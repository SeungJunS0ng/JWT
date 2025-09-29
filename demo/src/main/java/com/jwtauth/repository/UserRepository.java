package com.jwtauth.repository;

import com.jwtauth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String username, @Param("usernameOrEmail") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    Page<User> findByRole(User.Role role, Pageable pageable);

    List<User> findByEnabled(Boolean enabled);

    List<User> findByAccountNonLocked(Boolean accountNonLocked);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :threshold OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.enabled = false WHERE u.lastLoginAt < :threshold OR u.lastLoginAt IS NULL")
    int disableInactiveUsers(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.username = :username")
    void updateLastLoginTime(@Param("username") String username, @Param("loginTime") LocalDateTime loginTime);
}
