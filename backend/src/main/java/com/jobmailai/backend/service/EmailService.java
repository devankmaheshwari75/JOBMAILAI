package com.jobmailai.backend.service;

import com.jobmailai.backend.entity.Email;
import com.jobmailai.backend.entity.User;
import com.jobmailai.backend.repository.EmailRepository;
import com.jobmailai.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmailService {

    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    @Autowired
    public EmailService(EmailRepository emailRepository, UserRepository userRepository) {
        this.emailRepository = emailRepository;
        this.userRepository = userRepository;
    }

    public List<Email> saveAllEmails(List<Email> emails, Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {

            User currUser = userOptional.get();

            emails.forEach((email) -> {
                email.setUser(currUser);
            });
            return emailRepository.saveAll(emails);
        } else {
            throw new RuntimeException("User not found with id" + userId);
        }
    }

    public List<Email> getEmailsForUser(Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User currUser = userOptional.get();
            List<Email> list = emailRepository.findByUserId(userId);
            return list;
        } else {
            throw new RuntimeException("User not found with user id" + userId);
        }
    }
}