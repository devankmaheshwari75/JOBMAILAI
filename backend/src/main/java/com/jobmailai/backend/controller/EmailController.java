package com.jobmailai.backend.controller;

import com.jobmailai.backend.entity.Email;
import com.jobmailai.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Email>> getMyEmails(@PathVariable Long userId) {
        List<Email> emails = emailService.getEmailsForUser(userId);
        return ResponseEntity.ok(emails);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<List<Email>> saveMailsOfUser(@RequestBody List<Email> emails, @PathVariable Long userId) {

        List<Email> savedEmails = emailService.saveAllEmails(emails, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmails);
    }
}
