package com.automotiva.ficha_tecnica.controller;

import com.automotiva.ficha_tecnica.audit.AuditService;
import com.automotiva.ficha_tecnica.exception.BadRequestException;
import com.automotiva.ficha_tecnica.security.JwtService;
import com.automotiva.ficha_tecnica.security.dto.LoginRequest;
import com.automotiva.ficha_tecnica.security.dto.RefreshTokenRequest;
import com.automotiva.ficha_tecnica.security.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserDetails user = (UserDetails) authentication.getPrincipal();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());

            auditService.register("AUTH_LOGIN_SUCCESS", "user:" + user.getUsername(), "Login realizado");

            return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken, "Bearer", 900));
        } catch (BadCredentialsException ex) {
            auditService.register("AUTH_LOGIN_FAILURE", "user:" + request.username(), "Credencial invalida");
            throw new BadRequestException("Credenciais invalidas");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            throw new BadRequestException("Refresh token invalido");
        }

        String username = jwtService.extractUsername(request.refreshToken());
        UserDetails user = userDetailsService.loadUserByUsername(username);

        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        auditService.register("AUTH_REFRESH", "user:" + username, "Token renovado");

        return ResponseEntity.ok(new TokenResponse(accessToken, newRefreshToken, "Bearer", 900));
    }
}
