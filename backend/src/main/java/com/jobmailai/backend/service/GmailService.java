package com.jobmailai.backend.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.jobmailai.backend.entity.Email;
import com.jobmailai.backend.entity.User;
import com.jobmailai.backend.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final EmailRepository emailRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public void fetchAndProcessEmails(User user) throws IOException {
        Gmail gmail = getGmailClient(user);

        // 1. Build the query to filter for job-related emails within the last year.
        LocalDate oneYearAgo = LocalDate.now().minusDays(365);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateFilter = "after:" + oneYearAgo.format(formatter);
        String keywords = "(subject:(\"interview\" OR \"assessment\" OR \"coding challenge\" OR \"offer\" OR \"next steps\" OR \"application received\") OR from:(careers@ OR jobs@ OR hiring@ OR recruiting@))";
        String exclusions = "-(\"job alert\" OR newsletter OR promotion OR unsubscribe)";
        String finalQuery = String.join(" ", keywords, exclusions, dateFilter);
        System.out.println("Using Gmail query: " + finalQuery);

        // 2. Fetch the list of message IDs that match the query.
        ListMessagesResponse listResponse = gmail.users().messages()
                .list("me")
                .setQ(finalQuery)
                .setMaxResults(1000L)
                .execute();

        List<Message> messages = listResponse.getMessages();

        if (messages == null || messages.isEmpty()) {
            System.out.println("No new job-related messages found.");
            return;
        }

        System.out.println("Found " + messages.size() + " potential messages. Processing...");
        for (Message message : messages) {
            // IMPORTANT: Check if we have already saved this email to avoid duplicates.
            if (emailRepository.existsByMessageId(message.getId())) {
                System.out.println("Skipping already saved message ID: " + message.getId());
                continue;
            }

            try {
                // 3. For each new message ID, fetch the full details.
                Message fullMessage = gmail.users().messages().get("me", message.getId()).setFormat("FULL").execute();
                Email emailEntity = new Email();

                // 4. Populate the Email entity with parsed data.
                emailEntity.setMessageId(fullMessage.getId());
                emailEntity.setUser(user);

                // FIX: Correctly convert the timestamp to LocalDateTime.
                emailEntity.setTimestamp(
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(fullMessage.getInternalDate()), ZoneId.systemDefault())
                );

                MessagePart payload = fullMessage.getPayload();
                if (payload.getHeaders() != null) {
                    for (var header : payload.getHeaders()) {
                        if ("Subject".equalsIgnoreCase(header.getName())) emailEntity.setSubject(header.getValue());
                        if ("From".equalsIgnoreCase(header.getName())) emailEntity.setSender(header.getValue());
                    }
                }

                // FIX: Use our robust recursive parser to get the email body.
                String body = parseBodyFromMessageParts(payload);
                emailEntity.setBody(body);

                // 5. Save the new email entity to the database.
                emailRepository.save(emailEntity);
                System.out.println("Saved email with subject: " + emailEntity.getSubject());
            } catch (Exception e) {
                System.err.println("Failed to process message ID: " + message.getId() + ". Error: " + e.getMessage());
                // The loop will continue to the next message.
            }
        }
    }

    private Gmail getGmailClient(User user) throws IOException {
        GoogleRefreshTokenRequest refreshTokenRequest = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                user.getGoogleRefreshToken(),
                clientId,
                clientSecret
        );
        TokenResponse tokenResponse = refreshTokenRequest.execute();
        String newAccessToken = tokenResponse.getAccessToken();

        GoogleCredential credential = new GoogleCredential().setAccessToken(newAccessToken);
        return new Gmail.Builder(new NetHttpTransport(), new GsonFactory(), credential)
                .setApplicationName("JobMailAI")
                .build();
    }

    private String parseBodyFromMessageParts(MessagePart payload) {
        if (payload == null) return "";

        // Case 1: The payload itself is the body (simple, non-multipart email)
        if (payload.getMimeType().startsWith("text/") && payload.getBody() != null && payload.getBody().getData() != null) {
            return new String(Base64.decodeBase64(payload.getBody().getData()), StandardCharsets.UTF_8);
        }

        // Case 2: The payload has parts (multipart email)
        if (payload.getParts() != null) {
            return findTextPartRecursive(payload.getParts());
        }

        return ""; // Return empty string if no body is found
    }

    private String findTextPartRecursive(List<MessagePart> parts) {
        for (MessagePart part : parts) {
            if ("text/plain".equals(part.getMimeType()) && part.getBody() != null && part.getBody().getData() != null) {
                return new String(Base64.decodeBase64(part.getBody().getData()), StandardCharsets.UTF_8);
            }
            if (part.getMimeType().startsWith("multipart/") && part.getParts() != null) {
                String found = findTextPartRecursive(part.getParts());
                if (found != null && !found.isEmpty()) {
                    return found;
                }
            }
        }
        return "";
    }
}