package com.jobmailai.backend.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.jobmailai.backend.entity.User;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GmailService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public void fetchAndProcessEmails(User user) throws IOException, java.io.IOException {
        System.out.println("Starting email fetch for user: " + user.getEmail());
        GoogleRefreshTokenRequest refreshTokenRequest = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                user.getGoogleRefreshToken(),
                clientId,
                clientSecret
        );

        TokenResponse tokenResponse = refreshTokenRequest.execute();
        String newAccessToken = tokenResponse.getAccessToken();
        System.out.println("Successfully obtained a new access token.");

        // Step 2: Use the new Access Token to build the Gmail API client
        GoogleCredential credential = new GoogleCredential().setAccessToken(newAccessToken);
        Gmail gmail = new Gmail.Builder(new NetHttpTransport(), new GsonFactory(), credential)
                .setApplicationName("JobMailAI")
                .build();

        System.out.println("Gmail client built successfully.");

        // Step 3: Make a test API call to Gmail (list the first 10 messages)
        String userId = "me"; // "me" is a special identifier for the authenticated user
        ListMessagesResponse listResponse = gmail.users().messages().list(userId).setMaxResults(10L).execute();
        List<Message> messages = listResponse.getMessages();

        if (messages == null || messages.isEmpty()) {
            System.out.println("No messages found.");
        } else {
            System.out.println("Found " + messages.size() + " messages:");
            for (Message message : messages) {
                // The 'message' object here only contains the ID. We'll get full details later.
                System.out.println("- Message ID: " + message.getId());
            }
        }
    }
}
