package com.example.login.controller;

import javax.servlet.http.HttpServletRequest;

import com.example.login.exception.ExceptionForm;
import com.example.login.exception.SystemUserException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class EndpointAdvice {

    @ExceptionHandler(SystemUserException.class)
    public ResponseEntity<ExceptionForm> CommonExceptionHandler(SystemUserException exception, HttpServletRequest request) {
        ExceptionForm exceptionform = new ExceptionForm(exception.getTimestamp(),
                exception.getStatus(),
                exception.getClass().getCanonicalName(),
                exception.getMessage(),
                request.getServletPath());
        return new ResponseEntity<>(exceptionform, exception.getStatus());
    }
}
