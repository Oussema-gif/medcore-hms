package com.medcore.hms.auth.filter;

import com.medcore.hms.security.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            UserDetailsService userDetailsService) {

        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        /*
         * No Bearer token.
         *
         * This is normal for public endpoints such as /auth/login,
         * /auth/refresh and /actuator/health.
         */
        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        /*
         * Never overwrite an already established authentication.
         */
        if (!StringUtils.hasText(token)
                || SecurityContextHolder
                        .getContext()
                        .getAuthentication() != null) {

            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username =
                    jwtTokenService.extractUsername(token);

            if (!StringUtils.hasText(username)) {
                LOGGER.debug(
                        "JWT rejected for {} {}: token has no username",
                        request.getMethod(),
                        request.getRequestURI());

                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (!jwtTokenService.isTokenValid(token, userDetails)) {
                LOGGER.debug(
                        "JWT rejected for {} {}: token is invalid for user '{}'",
                        request.getMethod(),
                        request.getRequestURI(),
                        username);

                SecurityContextHolder.clearContext();

                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            LOGGER.debug(
                    "JWT authentication established for user '{}' on {} {}",
                    username,
                    request.getMethod(),
                    request.getRequestURI());

        } catch (RuntimeException exception) {

            SecurityContextHolder.clearContext();

            LOGGER.debug(
                    "JWT authentication failed for {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}