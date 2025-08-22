package com.jobmailai.backend.repository;

import com.jobmailai.backend.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {

    List<Email> findByUserId(Long userId);

    boolean existsByMessageId(String messageId);
}
