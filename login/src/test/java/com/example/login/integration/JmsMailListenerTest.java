package com.example.login.integration;

import com.example.login.exception.SystemUserException;
import com.example.login.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JmsMailListener.class, EmailService.class, ObjectMapper.class})
public class JmsMailListenerTest {

    @MockBean
    private JmsTemplate jmsTemplate;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private JmsMailListener jmsMailListener;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageArgumentCaptor;

    @Test
    public void test() throws SystemUserException {
        String message = "{\"to\":\"to@gmail.com\",\"from\":\"from@gmail.com\",\"subject\":\"subject\",\"text\":\"text\"}";

        Mockito.doNothing().when(javaMailSender).send(ArgumentMatchers.any(SimpleMailMessage.class));

        jmsMailListener.receiveMailRequest(message);

        Mockito.verify(javaMailSender).send(simpleMailMessageArgumentCaptor.capture());
        Mockito.verifyNoMoreInteractions(javaMailSender);

        SimpleMailMessage simpleMailMessage = simpleMailMessageArgumentCaptor.getValue();
        Assert.assertNotNull(simpleMailMessage.getTo());
        Assert.assertEquals(1, simpleMailMessage.getTo().length);
        Assert.assertEquals("to@gmail.com", simpleMailMessage.getTo()[0]);
        Assert.assertEquals("from@gmail.com", simpleMailMessage.getFrom());
        Assert.assertEquals("subject", simpleMailMessage.getSubject());
        Assert.assertEquals("text", simpleMailMessage.getText());
    }
}
