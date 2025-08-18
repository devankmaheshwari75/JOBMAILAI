package com.jobmailai.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String googleId; // <-- ADD THIS: To store the unique 'sub' from Google. This is a more reliable unique key than email.

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String pictureUrl; // <-- ADD THIS: To store the URL for the user's profile picture.

    @Column(columnDefinition = "TEXT") // Refresh tokens can be very long
    private String googleRefreshToken; // <-- RENAME THIS: Much clearer than 'token'. We'll store Google's refresh token here later.

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Email> emailList = new ArrayList<>();
}
