package com.dipa.notefournote.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username '" + username + "' is already taken.");
    }

}