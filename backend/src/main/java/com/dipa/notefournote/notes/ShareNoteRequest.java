package com.dipa.notefournote.notes;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ShareNoteRequest(
        @NotEmpty(message = "La lista di utenti non può essere vuota")
        Set<String> usernames
) {}