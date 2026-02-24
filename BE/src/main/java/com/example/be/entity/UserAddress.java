package com.example.be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "province_id")
    private Integer provinceId;

    @Column(name = "province_name")
    private String provinceName;

    @Column(name = "district_id")
    private Integer districtId;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "ward_name")
    private String wardName;

    @Column(name = "specific_address")
    private String specificAddress;

    @Column(name = "is_default")
    private Boolean isDefault;

    @PrePersist
    public void prePersist() {
        if (isDefault == null) {
            isDefault = false;
        }
    }
}
