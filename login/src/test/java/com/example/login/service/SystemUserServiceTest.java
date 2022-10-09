package com.example.login.service;

import com.example.login.model.enums.UserRoleEnum;
import com.example.login.model.persistance.SystemUser;
import com.example.login.repository.SystemUserRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest // TODO: Find a way to initiate only the 2 required dependencies and mock the others
public class SystemUserServiceTest {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Autowired
    private SystemUserService systemUserService;

    private SystemUser systemUser;

    @Before
    public void init() {
        systemUser = new SystemUser();
        systemUser.setEmail("test@gmail.com");
        systemUser.setPassword("password");
        systemUser.setRole(UserRoleEnum.USER);
        systemUser.setEnabled(true);
        systemUser.setLocked(false);
        systemUser.setExpired(false);
        systemUserRepository.saveAndFlush(systemUser);
    }

    @After
    public void after() {
        systemUserRepository.deleteAll();
    }

    @Test
    public void loadUserByUsername() throws UsernameNotFoundException {
        SystemUser databaseSystemUser = (SystemUser) systemUserService.loadUserByUsername(systemUser.getEmail());

        Assert.assertNotNull(databaseSystemUser);
        Assert.assertEquals(systemUser.getEmail(), databaseSystemUser.getEmail());
        Assert.assertNotNull(databaseSystemUser.getPassword());
        Assert.assertEquals(UserRoleEnum.USER, databaseSystemUser.getRole());
        Assert.assertTrue(databaseSystemUser.isEnabled());
        Assert.assertTrue(databaseSystemUser.isAccountNonLocked());
        Assert.assertTrue(databaseSystemUser.isAccountNonExpired());
        Assert.assertTrue(databaseSystemUser.isCredentialsNonExpired());
    }
}
