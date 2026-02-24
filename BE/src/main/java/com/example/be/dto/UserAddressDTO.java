package com.example.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressDTO {
    private Long id;
    private Long userId; // Only used for response or linking
    @jakarta.validation.constraints.NotBlank(message = "Receiver name is required")
    private String receiverName;
    @jakarta.validation.constraints.NotBlank(message = "Receiver phone is required")
    @jakarta.validation.constraints.Pattern(regexp = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$", message = "Invalid phone number")
    private String receiverPhone;
    @jakarta.validation.constraints.NotNull(message = "Province is required")
    private Integer provinceId;
    private String provinceName;
    @jakarta.validation.constraints.NotNull(message = "District is required")
    private Integer districtId;
    private String districtName;
    @jakarta.validation.constraints.NotBlank(message = "Ward is required")
    private String wardCode;
    private String wardName;
    @jakarta.validation.constraints.NotBlank(message = "Specific address is required")
    private String specificAddress;
    private Boolean isDefault;
}
