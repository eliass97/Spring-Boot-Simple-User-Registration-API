package com.example.login.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.login.model.persistance.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, Long> {
    Optional<RegistrationToken> findByToken(String token);

    List<RegistrationToken> findBySystemUserId(Long userId);

    List<RegistrationToken> findByExpirationDateBefore(LocalDateTime date);
}
