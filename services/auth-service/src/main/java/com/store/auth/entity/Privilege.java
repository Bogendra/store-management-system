package com.store.auth.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a privilege (fine-grained permission) in the system.
 */
@Entity
@Table(name = "privileges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
