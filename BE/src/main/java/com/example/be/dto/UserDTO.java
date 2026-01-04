package com.example.be.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private int rewardPoints;
    private String membershipTier;
    // Password generally not returned in DTO
}
