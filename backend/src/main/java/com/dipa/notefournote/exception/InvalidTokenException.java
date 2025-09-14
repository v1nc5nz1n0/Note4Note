package com.dipa.notefournote.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String tokenType) {
        super(tokenType + " token is not valid.");
    }

}