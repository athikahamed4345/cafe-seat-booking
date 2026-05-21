package com.restaurant.booking.config;

import com.restaurant.booking.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Handles all JWT logic in one place:
 *  - createToken()     : called by AuthService after login/register
 *  - doFilterInternal(): runs on every request to validate the token
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")     private String secret;
    @Value("${jwt.expiration}") private long   expiration;

    private final UserRepository userRepo;

    public JwtFilter(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // Build and sign a new JWT token for the given email
    public String createToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .compact();
    }

    // Read the email stored inside a token
    private String readEmail(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    // Runs automatically on every HTTP request
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = readEmail(header.substring(7));
                // Look up user in DB and mark them as logged in for this request
                userRepo.findByEmail(email).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                        email, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (Exception ignored) {
                // Invalid token — request continues as unauthenticated
            }
        }

        chain.doFilter(req, res);
    }
}
