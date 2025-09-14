package com.dipa.notefournote.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
public class UserController {

    private final UserService userService;

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

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.debug("Received login request for user: '{}'", request.username());
        final String username = request.username();

        final UserLogged jwtTokens = userService.loginUser(username, request.password());

        log.debug("Returned JWT tokens for user: {}", username);
        return ResponseEntity.ok(new UserLoginResponse(jwtTokens.accessToken(), jwtTokens.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Received refresh token request");

        final UserLogged userLogged = userService.refreshToken(request.refreshToken());

        log.debug("Returned new JWT tokens after refresh");
        return ResponseEntity.ok(new UserLoginResponse(userLogged.accessToken(), userLogged.refreshToken()));
    }

}