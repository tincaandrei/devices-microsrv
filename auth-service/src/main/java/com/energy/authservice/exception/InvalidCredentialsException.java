package com.energy.authservice.exception;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
