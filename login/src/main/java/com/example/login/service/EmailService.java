package com.example.login.service;

import com.example.login.exception.SystemUserException;
import com.example.login.model.dto.MailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String username;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private JavaMailSender mailSender;

    public void sendMailToBroker(String address, String subject, String text) throws SystemUserException {
        MailRequest mailRequest = new MailRequest(address, username, subject, text);
        String message;
        try {
            message = objectMapper.writeValueAsString(mailRequest);
        } catch (JsonProcessingException e) {
            throw new SystemUserException(HttpStatus.BAD_REQUEST, "Failed to serialize message for broker!");
        }

        LOGGER.info("Sending message to broker: {}", message);
        jmsTemplate.convertAndSend("mail", message);
    }

    @Async
    public void sendMail(MailRequest mailRequest) {
        LOGGER.info("Sending mail: {}", mailRequest);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(mailRequest.getTo());
        simpleMailMessage.setFrom(mailRequest.getFrom());
        simpleMailMessage.setSubject(mailRequest.getSubject());
        simpleMailMessage.setText(mailRequest.getText());
        mailSender.send(simpleMailMessage);
    }
}
