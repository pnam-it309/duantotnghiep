package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;

    @Column(name = "reward_points")
    private int rewardPoints; // Total available points

    @Column(name = "membership_tier")
    private String membershipTier; // SILVER, GOLD, DIAMOND

    @PrePersist
    protected void onPrePersist() {
        if (membershipTier == null)
            membershipTier = "SILVER";
    }
}
