package com.ledgepro.controller;

import com.ledgepro.model.AuthResponse;
import com.ledgepro.model.LoginRequest;
import com.ledgepro.model.RegisterRequest;
import com.ledgepro.service.AuthService;
import com.ledgepro.util.GenericResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public GenericResponse<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return GenericResponse.success(response, response.getMessage());
    }

    @PostMapping("/register")
    public GenericResponse<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return GenericResponse.success(response, response.getMessage());
    }
}
