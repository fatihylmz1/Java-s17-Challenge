package com.workintech.alexgrade.alexgrade.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCreditValueException extends CourseException {
    private HttpStatus status;
    public InvalidCreditValueException(String message, HttpStatus status) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
