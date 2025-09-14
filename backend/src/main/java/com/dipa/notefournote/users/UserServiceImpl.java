package com.dipa.notefournote.users;

import com.dipa.notefournote.exception.InvalidTokenException;
import com.dipa.notefournote.exception.UsernameAlreadyExistsException;
import com.dipa.notefournote.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public User registerUser(String username, String password) {
        log.info("Registering new user with username: '{}'", username);

        userRepository.findByUsername(username).ifPresent(
                user -> {
                    throw new UsernameAlreadyExistsException(username);
        });

        final User userDomain = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        final UserEntity userEntity = userMapper.toEntity(userDomain);

        final UserEntity savedEntity = userRepository.save(userEntity);

        log.info("Registered new user '{}' with ID: {}", username, savedEntity.getId());
        return userMapper.toModel(savedEntity);
    }

    @Override
    public UserLogged loginUser(String username, String password) {
        log.info("Login user with username: '{}'", username);

        // A credential mismatch will throw a BadCredentialsException (handled in GlobalExceptionHandler with HTTP 401)
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String accessToken = jwtService.generateToken(authentication, false);
        final String refreshToken = jwtService.generateToken(authentication, true);

        log.info("Logged in user with username: '{}'", username);
        return new UserLogged(accessToken, refreshToken);
    }

    @Override
    public UserLogged refreshToken(String refreshToken) {

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidTokenException("Refresh");
        }

        final String username = jwtService.getUsernameFromToken(refreshToken);
        log.debug("Refreshing token for user: '{}'", username);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        final String newAccessToken = jwtService.generateToken(authentication, false);
        final String newRefreshToken = jwtService.generateToken(authentication, true);

        log.info("Successfully refreshed tokens for user: '{}'", username);
        return new UserLogged(newAccessToken, newRefreshToken);
    }

}