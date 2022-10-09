package com.example.login.exception;

import java.sql.Timestamp;

import org.springframework.http.HttpStatus;

public class SystemUserException extends Exception {

    private HttpStatus status;
    private final Timestamp timestamp;

    public SystemUserException() {
        super();
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public SystemUserException(String message) {
        super(message);
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public SystemUserException(HttpStatus status) {
        super();
        timestamp = new Timestamp(System.currentTimeMillis());
        this.status = status;
    }

    public SystemUserException(HttpStatus status, String message) {
        super(message);
        timestamp = new Timestamp(System.currentTimeMillis());
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}