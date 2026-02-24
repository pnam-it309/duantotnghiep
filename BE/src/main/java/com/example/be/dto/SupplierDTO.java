package com.example.be.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Long id;
    @jakarta.validation.constraints.NotBlank(message = "Supplier name is required")
    private String name;
    private String address;
    @jakarta.validation.constraints.Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Invalid phone number")
    private String phoneNumber;
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;
    private Boolean active;
}
