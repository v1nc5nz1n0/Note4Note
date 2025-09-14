package com.dipa.notefournote.notes;

import jakarta.validation.constraints.NotBlank;

public record ShareNoteRequest(
        @NotBlank(message = "L'username dell'utente con cui condividere la nota non può essere vuoto.")
        String username
) {}