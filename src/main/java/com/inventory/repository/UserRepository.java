package com.inventory.repository;

import com.inventory.entity.Role;
import com.inventory.entity.User;
import com.inventory.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    long countByStatus(UserStatus status);
    List<User> findAllByOrderByCreatedAtDesc();
}
