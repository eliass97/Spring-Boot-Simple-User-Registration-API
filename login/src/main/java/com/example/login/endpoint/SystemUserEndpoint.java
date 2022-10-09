package com.example.login.endpoint;

import com.example.login.exception.SystemUserException;
import com.example.login.model.dto.SystemUserDTO;
import com.example.login.service.SystemUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class SystemUserEndpoint {

    private final SystemUserService systemUserService;

    public SystemUserEndpoint(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody SystemUserDTO systemUserDTO) throws SystemUserException {
        systemUserService.registerUser(systemUserDTO);
    }

    @GetMapping("/validate")
    public void validateUser(@RequestParam("token") String token) throws SystemUserException {
        systemUserService.validateUser(token);
    }

    @DeleteMapping("/delete")
    public void deleteUser(@RequestBody SystemUserDTO systemUserDTO) throws SystemUserException {
        systemUserService.deleteUser(systemUserDTO);
    }
}
