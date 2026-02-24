package com.example.be.service;

import com.example.be.dto.auth.JwtResponse;
import com.example.be.dto.auth.LoginRequest;
import com.example.be.dto.auth.RegisterRequest;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);

    String register(RegisterRequest registerRequest);
}
