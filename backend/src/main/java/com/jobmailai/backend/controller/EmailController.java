package com.jobmailai.backend.controller;

import com.jobmailai.backend.entity.User;
import com.jobmailai.backend.repository.UserRepository;
import com.jobmailai.backend.service.GmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final GmailService gmailService;
    private final UserRepository userRepository;

    @GetMapping("/sync")
    // CHANGE THE PARAMETER TYPE FROM Jwt to UserDetails
    public ResponseEntity<String> syncEmails(@AuthenticationPrincipal UserDetails userDetails) {

        // The user's email is now in userDetails.getUsername()
        String userEmail = userDetails.getUsername();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        if (user.getGoogleRefreshToken() == null) {
            return ResponseEntity.badRequest().body("User has not granted offline access or refresh token is missing.");
        }

        try {
            gmailService.fetchAndProcessEmails(user);
            return ResponseEntity.ok("Email sync started successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error syncing emails: " + e.getMessage());
        }
    }
}