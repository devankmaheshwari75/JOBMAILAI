package com.jobmailai.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is a crucial field to prevent saving the same email multiple times.
    // It will store the unique message ID from Gmail.
    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(length = 512) // It's good practice to set a length for strings
    private String subject;

    @Column(length = 255)
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String body;

    // This field is now correctly handled by Lombok.
    private LocalDateTime timestamp;

    // Setting a default value is better than allowing nulls.
    @Column(nullable = false)
    private boolean jobFlag = false;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetching is often better for performance
    @JoinColumn(name = "user_id", nullable = false) // Naming the foreign key column is good practice
    @JsonBackReference
    private User user;
}
