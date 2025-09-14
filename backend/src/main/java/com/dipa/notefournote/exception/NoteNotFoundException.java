package com.dipa.notefournote.exception;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String message) {
        super(message);
    }

}