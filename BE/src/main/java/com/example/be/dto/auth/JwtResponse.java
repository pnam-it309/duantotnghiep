package com.example.be.dto.auth;

import com.example.be.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDTO user;
}
