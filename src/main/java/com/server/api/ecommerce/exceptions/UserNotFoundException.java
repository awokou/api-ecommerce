package com.server.api.ecommerce.exceptions;

import java.io.Serial;

public class UserNotFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotFoundException() {}

    public UserNotFoundException(String message) {
        super(message);
    }

}
