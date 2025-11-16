package org.example.userservice.exception.types;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
