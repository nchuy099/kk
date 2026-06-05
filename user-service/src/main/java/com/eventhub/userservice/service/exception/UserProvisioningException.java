package com.eventhub.userservice.service.exception;

public class UserProvisioningException extends RuntimeException {
    public UserProvisioningException(String message) {
        super(message);
    }

    public UserProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
