package com.bytebridges.anytop.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytebridges.anytop.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
