package com.dipa.notefournote.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank(message = "Il nome utente non può essere vuoto")
        @Size(min = 3, max = 20, message = "Il nome utente deve avere tra 3 e 20 caratteri")
        String username,

        @NotBlank(message = "La password non può essere vuota")
        @Size(min = 8, max = 100, message = "La password deve avere almeno 8 caratteri")
        String password
) {}