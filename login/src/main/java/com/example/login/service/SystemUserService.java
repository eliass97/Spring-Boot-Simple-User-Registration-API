package com.example.login.service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.login.exception.SystemUserException;
import com.example.login.model.dto.SystemUserDTO;
import com.example.login.model.enums.UserRoleEnum;
import com.example.login.model.persistance.RegistrationToken;
import com.example.login.model.persistance.SystemUser;
import com.example.login.repository.SystemUserRepository;
import com.example.login.util.MailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SystemUserService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUserService.class);

    @Value("${constants.mail-confirmation-message}")
    private String mailConfirmationMessage;

    @Value("${constants.mail-deletion-message}")
    private String mailDeletionMessage;

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RegistrationTokenService registrationTokenService;

    @Autowired
    private EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return systemUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    @Transactional
    public void registerUser(SystemUserDTO systemUserDTO) throws SystemUserException {
        if (systemUserDTO.getEmail() == null || systemUserDTO.getPassword() == null) {
            throw new SystemUserException(HttpStatus.BAD_REQUEST, "Provided e-mail or password is empty!");
        }

        boolean isMail = MailValidator.isMail(systemUserDTO.getEmail());
        if (!isMail) {
            throw new SystemUserException(HttpStatus.BAD_REQUEST, "Provided e-mail is invalid!");
        }

        SystemUser savedSystemUser;
        Optional<SystemUser> databaseUser = systemUserRepository.findByEmail(systemUserDTO.getEmail());
        if (databaseUser.isPresent()) {
            savedSystemUser = databaseUser.get();
            LOGGER.info("Received registration request for user who already exists in the database: {}", savedSystemUser.getEmail());
            if (savedSystemUser.getEnabled()) {
                throw new SystemUserException(HttpStatus.CONFLICT, "User with specified email already exists!");
            }
        } else {
            SystemUser systemUser = new SystemUser();
            systemUser.setRole(UserRoleEnum.USER);
            systemUser.setEmail(systemUserDTO.getEmail());
            systemUser.setPassword(passwordEncoder.encode(systemUserDTO.getPassword()));
            savedSystemUser = systemUserRepository.save(systemUser);
            LOGGER.info("Created new system user: {}", savedSystemUser);
        }

        RegistrationToken registrationToken = registrationTokenService.createToken(savedSystemUser);
        emailService.sendMailToBroker(savedSystemUser.getEmail(), "Confirm your registration",
                String.format(mailConfirmationMessage, registrationToken.getToken()));
    }

    public void validateUser(String token) throws SystemUserException {
        LOGGER.info("Received validation request for token: {}", token);
        RegistrationToken registrationToken = registrationTokenService.getByToken(token);

        boolean isExpired = LocalDateTime.now().isAfter(registrationToken.getExpirationDate());
        if (isExpired) {
            throw new SystemUserException(HttpStatus.BAD_REQUEST, "Registration token has expired!");
        }

        SystemUser systemUser = registrationToken.getSystemUser();
        systemUser.setEnabled(true);
        LOGGER.info("Activating user: {}", systemUser.getEmail());
        systemUserRepository.save(systemUser);
    }

    @Transactional
    public void deleteUser(SystemUserDTO systemUserDTO) throws SystemUserException {
        if (systemUserDTO.getEmail() == null || systemUserDTO.getPassword() == null) {
            throw new SystemUserException(HttpStatus.BAD_REQUEST, "Provided e-mail or password is empty!");
        }

        SystemUser systemUser = systemUserRepository.findByEmail(systemUserDTO.getEmail())
                .orElseThrow(() -> new SystemUserException(HttpStatus.NOT_FOUND, "User with provided e-mail does not exist!"));

        List<RegistrationToken> registrationTokenList = registrationTokenService.getByUserId(systemUser.getId());
        String email = systemUser.getEmail();
        LOGGER.info("Deleting registration tokens for user: {}", systemUser.getEmail());
        for (RegistrationToken registrationToken : registrationTokenList) {
            registrationTokenService.deleteById(registrationToken.getId());
        }

        systemUserRepository.deleteById(systemUser.getId());
        LOGGER.info("User {} has been deleted", email);

        emailService.sendMailToBroker(email, "Account deletion", mailDeletionMessage);
    }
}
