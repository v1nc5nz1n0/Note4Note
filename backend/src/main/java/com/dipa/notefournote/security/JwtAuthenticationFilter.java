package com.dipa.notefournote.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Solo gli access token consentono l'accesso alle risorse
            if (jwtService.isAccessTokenValid(jwt)) {
                try {
                    final String username = jwtService.getUsernameFromToken(jwt);
                    final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    final JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("User '{}' authenticated successfully", username);
                } catch (Exception e) {
                    log.error("Could not set user authentication in security context", e);
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.warn("Received an invalid, expired, or non-access JWT token");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        final String bearerToken = request.getHeader(HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

}