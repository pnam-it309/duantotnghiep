package com.example.be.service.impl;

import com.example.be.dto.UserDTO;
import com.example.be.dto.auth.JwtResponse;
import com.example.be.dto.auth.LoginRequest;
import com.example.be.dto.auth.RegisterRequest;
import com.example.be.entity.User;
import com.example.be.util.DtoMapper;
import com.example.be.repository.UserRepository;
import com.example.be.security.JwtTokenProvider;
import com.example.be.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DtoMapper dtoMapper;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        UserDTO userDTO = dtoMapper.toUserDTO(user);

        return new JwtResponse(token, "Bearer", userDTO);
    }

    @Override
    public String register(RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setRole("USER");
        user.setMembershipTier("SILVER");

        userRepository.save(user);

        return "User registered successfully!.";
    }
}
