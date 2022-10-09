package com.example.login.model.enums;

public enum UserRoleEnum {
    USER("USER_ROLE_USER"),
    ADMIN("USER_ROLE_ADMIN");

    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }
}
