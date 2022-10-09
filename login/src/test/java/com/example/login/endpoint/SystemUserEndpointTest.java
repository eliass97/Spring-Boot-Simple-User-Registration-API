package com.example.login.endpoint;

import java.time.LocalDateTime;
import java.util.List;

import com.example.login.model.dto.SystemUserDTO;
import com.example.login.model.enums.UserRoleEnum;
import com.example.login.model.persistance.RegistrationToken;
import com.example.login.model.persistance.SystemUser;
import com.example.login.repository.RegistrationTokenRepository;
import com.example.login.repository.SystemUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class SystemUserEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private JmsTemplate jmsTemplate;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private SystemUserDTO systemUserDTO;

    @Before
    public void init() {
        systemUserDTO = new SystemUserDTO();
        systemUserDTO.setEmail("test@gmail.com");
        systemUserDTO.setPassword("password");
    }

    @After
    public void after() {
        registrationTokenRepository.deleteAll();
        systemUserRepository.deleteAll();
    }

    @Test
    public void registerUser() throws Exception {
        String body = objectMapper.writeValueAsString(systemUserDTO);

        Mockito.doNothing().when(jmsTemplate).convertAndSend(ArgumentMatchers.eq("mail"), ArgumentMatchers.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(jmsTemplate).convertAndSend(ArgumentMatchers.eq("mail"), ArgumentMatchers.anyString());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertEquals(1, systemUsers.size());
        Assert.assertEquals(systemUserDTO.getEmail(), systemUsers.get(0).getEmail());
        Assert.assertTrue(bCryptPasswordEncoder.matches(systemUserDTO.getPassword(), systemUsers.get(0).getPassword()));
        Assert.assertEquals(UserRoleEnum.USER, systemUsers.get(0).getRole());
        Assert.assertFalse(systemUsers.get(0).isEnabled());
        Assert.assertTrue(systemUsers.get(0).isAccountNonLocked());
        Assert.assertTrue(systemUsers.get(0).isAccountNonExpired());
        Assert.assertTrue(systemUsers.get(0).isCredentialsNonExpired());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertEquals(1, registrationTokens.size());
        Assert.assertNotNull(registrationTokens.get(0).getSystemUser());
        Assert.assertEquals(systemUserDTO.getEmail(), registrationTokens.get(0).getSystemUser().getEmail());
    }

    @Test
    public void registerUserWithAlreadyExistingSystemUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setEmail(systemUserDTO.getEmail());
        systemUser.setPassword(systemUserDTO.getPassword());
        systemUser.setRole(UserRoleEnum.USER);
        systemUser.setEnabled(false);
        systemUser.setLocked(false);
        systemUser.setExpired(false);
        systemUserRepository.saveAndFlush(systemUser);

        String body = objectMapper.writeValueAsString(systemUserDTO);

        Mockito.doNothing().when(jmsTemplate).convertAndSend(ArgumentMatchers.eq("mail"), ArgumentMatchers.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(jmsTemplate).convertAndSend(ArgumentMatchers.eq("mail"), ArgumentMatchers.anyString());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertEquals(1, systemUsers.size());
        Assert.assertEquals(systemUserDTO.getEmail(), systemUsers.get(0).getEmail());
        Assert.assertEquals(systemUserDTO.getPassword(), systemUsers.get(0).getPassword());
        Assert.assertEquals(UserRoleEnum.USER, systemUsers.get(0).getRole());
        Assert.assertFalse(systemUsers.get(0).isEnabled());
        Assert.assertTrue(systemUsers.get(0).isAccountNonLocked());
        Assert.assertTrue(systemUsers.get(0).isAccountNonExpired());
        Assert.assertTrue(systemUsers.get(0).isCredentialsNonExpired());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertEquals(1, registrationTokens.size());
        Assert.assertNotNull(registrationTokens.get(0).getSystemUser());
        Assert.assertEquals(systemUserDTO.getEmail(), registrationTokens.get(0).getSystemUser().getEmail());
    }

    @Test
    public void registerUserWithNullEmail() throws Exception {
        systemUserDTO.setEmail(null);
        String body = objectMapper.writeValueAsString(systemUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertTrue(systemUsers.isEmpty());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertTrue(registrationTokens.isEmpty());
    }

    @Test
    public void registerUserWithNullPassword() throws Exception {
        systemUserDTO.setPassword(null);
        String body = objectMapper.writeValueAsString(systemUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertTrue(systemUsers.isEmpty());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertTrue(registrationTokens.isEmpty());
    }

    @Test
    public void registerUserWithInvalidEmail() throws Exception {
        systemUserDTO.setEmail("johnMail.com");
        String body = objectMapper.writeValueAsString(systemUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertTrue(systemUsers.isEmpty());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertTrue(registrationTokens.isEmpty());
    }

    @Test
    public void registerUserWithAlreadyExistingEnabledSystemUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setEmail(systemUserDTO.getEmail());
        systemUser.setPassword(systemUserDTO.getPassword());
        systemUser.setRole(UserRoleEnum.USER);
        systemUser.setEnabled(true);
        systemUser.setLocked(false);
        systemUser.setExpired(false);
        systemUserRepository.saveAndFlush(systemUser);

        String body = objectMapper.writeValueAsString(systemUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertEquals(1, systemUsers.size());
        Assert.assertEquals(systemUserDTO.getEmail(), systemUsers.get(0).getEmail());
        Assert.assertEquals(systemUser.getPassword(), systemUsers.get(0).getPassword());
        Assert.assertEquals(UserRoleEnum.USER, systemUsers.get(0).getRole());
        Assert.assertTrue(systemUsers.get(0).isEnabled());
        Assert.assertTrue(systemUsers.get(0).isAccountNonLocked());
        Assert.assertTrue(systemUsers.get(0).isAccountNonExpired());
        Assert.assertTrue(systemUsers.get(0).isCredentialsNonExpired());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertTrue(registrationTokens.isEmpty());
    }

    @Test
    public void validateUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setEmail(systemUserDTO.getEmail());
        systemUser.setPassword(systemUserDTO.getPassword());
        systemUser.setRole(UserRoleEnum.USER);
        systemUser.setEnabled(false);
        systemUser.setLocked(false);
        systemUser.setExpired(false);
        systemUserRepository.saveAndFlush(systemUser);

        RegistrationToken registrationToken = new RegistrationToken();
        registrationToken.setSystemUser(systemUser);
        registrationToken.setToken("token1");
        registrationToken.setCreationDate(LocalDateTime.now().minusMinutes(2));
        registrationToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        registrationTokenRepository.saveAndFlush(registrationToken);

        RegistrationToken otherRegistrationToken = new RegistrationToken();
        otherRegistrationToken.setSystemUser(systemUser);
        otherRegistrationToken.setToken("token2");
        otherRegistrationToken.setCreationDate(LocalDateTime.now().minusDays(5));
        otherRegistrationToken.setExpirationDate(LocalDateTime.now().minusDays(2));
        registrationTokenRepository.saveAndFlush(otherRegistrationToken);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/validate?token=token1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertEquals(1, systemUsers.size());
        Assert.assertEquals(systemUser.getEmail(), systemUsers.get(0).getEmail());
        Assert.assertEquals(systemUser.getPassword(), systemUsers.get(0).getPassword());
        Assert.assertTrue(systemUsers.get(0).isEnabled());
        Assert.assertTrue(systemUsers.get(0).isAccountNonLocked());
        Assert.assertTrue(systemUsers.get(0).isAccountNonExpired());
        Assert.assertTrue(systemUsers.get(0).isCredentialsNonExpired());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertEquals(2, registrationTokens.size());
    }

    @Test
    public void validateUserWithExpiredToken() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setEmail(systemUserDTO.getEmail());
        systemUser.setPassword(systemUserDTO.getPassword());
        systemUser.setRole(UserRoleEnum.USER);
        systemUser.setEnabled(false);
        systemUser.setLocked(false);
        systemUser.setExpired(false);
        systemUserRepository.saveAndFlush(systemUser);

        RegistrationToken registrationToken = new RegistrationToken();
        registrationToken.setSystemUser(systemUser);
        registrationToken.setToken("token1");
        registrationToken.setCreationDate(LocalDateTime.now().minusMinutes(20));
        registrationToken.setExpirationDate(LocalDateTime.now().minusMinutes(5));
        registrationTokenRepository.saveAndFlush(registrationToken);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/validate?token=token1"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertEquals(1, systemUsers.size());
        Assert.assertEquals(systemUser.getEmail(), systemUsers.get(0).getEmail());
        Assert.assertEquals(systemUser.getPassword(), systemUsers.get(0).getPassword());
        Assert.assertFalse(systemUsers.get(0).isEnabled());
        Assert.assertTrue(systemUsers.get(0).isAccountNonLocked());
        Assert.assertTrue(systemUsers.get(0).isAccountNonExpired());
        Assert.assertTrue(systemUsers.get(0).isCredentialsNonExpired());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertEquals(1, registrationTokens.size());
    }

    @Test
    public void deleteUser() throws Exception {
        SystemUser systemUser = new SystemUser();
        systemUser.setEmail(systemUserDTO.getEmail());
        systemUser.setPassword(systemUserDTO.getPassword());
        systemUser.setRole(UserRoleEnum.USER);
        systemUser.setEnabled(true);
        systemUser.setLocked(false);
        systemUser.setExpired(false);
        systemUserRepository.saveAndFlush(systemUser);

        RegistrationToken registrationToken = new RegistrationToken();
        registrationToken.setSystemUser(systemUser);
        registrationToken.setToken("token1");
        registrationToken.setCreationDate(LocalDateTime.now().minusDays(2));
        registrationToken.setExpirationDate(LocalDateTime.now().minusDays(1));
        registrationTokenRepository.saveAndFlush(registrationToken);

        SystemUser otherSystemUser = new SystemUser();
        otherSystemUser.setEmail("other@gmail.com");
        otherSystemUser.setPassword("otherPassword");
        otherSystemUser.setRole(UserRoleEnum.USER);
        otherSystemUser.setEnabled(true);
        otherSystemUser.setLocked(false);
        otherSystemUser.setExpired(false);
        systemUserRepository.saveAndFlush(otherSystemUser);

        RegistrationToken otherRegistrationToken = new RegistrationToken();
        otherRegistrationToken.setSystemUser(otherSystemUser);
        otherRegistrationToken.setToken("token2");
        otherRegistrationToken.setCreationDate(LocalDateTime.now().minusDays(5));
        otherRegistrationToken.setExpirationDate(LocalDateTime.now().minusDays(2));
        registrationTokenRepository.saveAndFlush(otherRegistrationToken);

        String body = objectMapper.writeValueAsString(systemUserDTO);

        Mockito.doNothing().when(jmsTemplate).convertAndSend(ArgumentMatchers.eq("mail"), ArgumentMatchers.anyString());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/delete")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(jmsTemplate).convertAndSend(ArgumentMatchers.eq("mail"), ArgumentMatchers.anyString());

        List<SystemUser> systemUsers = systemUserRepository.findAll();
        Assert.assertEquals(1, systemUsers.size());
        Assert.assertEquals(otherSystemUser.getEmail(), systemUsers.get(0).getEmail());

        List<RegistrationToken> registrationTokens = registrationTokenRepository.findAll();
        Assert.assertEquals(1, registrationTokens.size());
        Assert.assertEquals(otherRegistrationToken.getToken(), registrationTokens.get(0).getToken());
    }

    @Test
    public void deleteUserWithNullEmail() throws Exception {
        systemUserDTO.setEmail(null);
        String body = objectMapper.writeValueAsString(systemUserDTO);


        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/delete")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void deleteUserWithNullPassword() throws Exception {
        systemUserDTO.setPassword(null);
        String body = objectMapper.writeValueAsString(systemUserDTO);


        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/delete")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void deleteUserWithNonExistingSystemUser() throws Exception {
        String body = objectMapper.writeValueAsString(systemUserDTO);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/user/delete")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
