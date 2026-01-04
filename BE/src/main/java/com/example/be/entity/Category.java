package com.example.be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Use JSON ignore to prevent infinite recursion if serializing, or just rely on
    // DTOs
    // @OneToMany(mappedBy = "category")
    // private List<Product> products;
}
