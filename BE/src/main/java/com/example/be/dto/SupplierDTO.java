package com.example.be.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean active;
}
