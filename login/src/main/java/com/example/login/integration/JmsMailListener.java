package com.example.login.integration;

import com.example.login.exception.SystemUserException;
import com.example.login.model.dto.MailRequest;
import com.example.login.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class JmsMailListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

    @JmsListener(destination = "mail")
    public void receiveMailRequest(String message) throws SystemUserException {
        MailRequest mailRequest;
        try {
            mailRequest = objectMapper.readValue(message, MailRequest.class);
        } catch (JsonProcessingException e) {
            throw new SystemUserException(HttpStatus.BAD_REQUEST, "Failed to deserialize message from broker!");
        }
        emailService.sendMail(mailRequest);
    }
}
