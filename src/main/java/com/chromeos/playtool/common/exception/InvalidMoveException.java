package com.chromeos.playtool.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidMoveException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidMoveException(String message) {
        super(message);
    }
}
