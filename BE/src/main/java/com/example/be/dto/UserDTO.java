package com.example.be.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Username is required")
    private String username;

    @jakarta.validation.constraints.Email(message = "Invalid email format")
    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    private String email;

    private Integer rewardPoints;
    private String membershipTier;

    @jakarta.validation.constraints.NotBlank(message = "Full name is required")
    private String fullName;

    @jakarta.validation.constraints.Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Invalid phone number")
    private String phoneNumber;

    private String address;
    private String role;
    private String password;
}
