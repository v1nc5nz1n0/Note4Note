package com.dipa.notefournote.exception;

public class NoteAccessDeniedException extends RuntimeException {

    public NoteAccessDeniedException(String message) {
        super(message);
    }

}