package com.example.login.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.login.exception.SystemUserException;
import com.example.login.model.persistance.RegistrationToken;
import com.example.login.model.persistance.SystemUser;
import com.example.login.repository.RegistrationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RegistrationTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationTokenService.class);

    @Value("${constants.token-expiration-minutes}")
    private Long tokenExpirationMinutes;

    private final RegistrationTokenRepository registrationTokenRepository;

    public RegistrationTokenService(RegistrationTokenRepository registrationTokenRepository) {
        this.registrationTokenRepository = registrationTokenRepository;
    }

    protected RegistrationToken createToken(SystemUser systemUser) {
        LOGGER.info("Creating token for user: {}", systemUser.getEmail());
        RegistrationToken registrationToken = new RegistrationToken();
        registrationToken.setToken(UUID.randomUUID().toString());
        registrationToken.setCreationDate(LocalDateTime.now());
        registrationToken.setExpirationDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        registrationToken.setSystemUser(systemUser);
        return registrationTokenRepository.save(registrationToken);
    }

    public RegistrationToken getByToken(String token) throws SystemUserException {
        return registrationTokenRepository.findByToken(token)
                .orElseThrow(() -> new SystemUserException(HttpStatus.NOT_FOUND, "Registration token does not exist!"));
    }

    public List<RegistrationToken> getByUserId(Long userId) {
        return registrationTokenRepository.findBySystemUserId(userId);
    }

    public void deleteById(Long id) {
        registrationTokenRepository.deleteById(id);
    }

    public List<RegistrationToken> getExpiredRegistrationTokens() {
        LocalDateTime now = LocalDateTime.now();
        return registrationTokenRepository.findByExpirationDateBefore(now);
    }
}
