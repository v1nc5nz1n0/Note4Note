package com.dipa.notefournote.notes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNoteRequest(
        @NotBlank(message = "Il titolo non può essere vuoto.")
        @Size(max = 100, message = "Il titolo non può superare i 100 caratteri.")
        String title,

        @NotBlank(message = "Il contenuto non può essere vuoto.")
        @Size(min = 10, message = "Il contenuto non può essere inferiore a 10 caratteri.")
        String content
) {}