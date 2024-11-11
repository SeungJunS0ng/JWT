package com.demo.repository;

import com.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}