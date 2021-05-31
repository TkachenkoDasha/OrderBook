package com.tkachenko.exception;

public class IllegalInputException extends RuntimeException {
    public IllegalInputException() {
    }

    public IllegalInputException(String message) {
        super(message);
    }
}
