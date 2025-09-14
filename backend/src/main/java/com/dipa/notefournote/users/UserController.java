package com.dipa.notefournote.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "1. Users & Authentication", description = "API per la registrazione e l'autenticazione degli utenti")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Registra un nuovo utente", description = "Crea un nuovo utente nel sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utente creato con successo"),
            @ApiResponse(responseCode = "400", description = "Errore di validazione nei dati inviati",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username già esistente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.debug("Received new registration request for user: '{}'", request.username());
        final String username = request.username();

        final User createdUser = userService.registerUser(username, request.password());

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();

        final UserRegistrationResponse responseDto = new UserRegistrationResponse(
                createdUser.getId(),
                createdUser.getUsername());

        log.debug("Registered new user '{}' to: {}", username, location);
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "Effettua il login di un utente", description = "Autentica un utente e restituisce un access token e un refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login effettuato con successo"),
            @ApiResponse(responseCode = "400", description = "Errore di validazione nei dati inviati",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenziali non valide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.debug("Received login request for user: '{}'", request.username());
        final String username = request.username();

        final UserLogged jwtTokens = userService.loginUser(username, request.password());

        log.debug("Returned JWT tokens for user: {}", username);
        return ResponseEntity.ok(new UserLoginResponse(jwtTokens.accessToken(), jwtTokens.refreshToken()));
    }

    @Operation(summary = "Aggiorna l'access token", description = "Genera un nuovo access token utilizzando un refresh token valido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token aggiornato con successo"),
            @ApiResponse(responseCode = "401", description = "Refresh token non valido o scaduto",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Received refresh token request");

        final UserLogged userLogged = userService.refreshToken(request.refreshToken());

        log.debug("Returned new JWT tokens after refresh");
        return ResponseEntity.ok(new UserLoginResponse(userLogged.accessToken(), userLogged.refreshToken()));
    }

}