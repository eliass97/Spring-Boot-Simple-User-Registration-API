package com.example.login.repository;

import java.util.Optional;

import com.example.login.model.persistance.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {

    Optional<SystemUser> findByEmail(String email);
}
