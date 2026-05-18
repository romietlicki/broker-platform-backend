package com.brokerplatform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 100)
    private String description;

    public static final String ROLE_BROKER = "ROLE_BROKER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
}
