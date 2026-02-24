package com.example.be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        // [MOCK] Integration with JavaMailSender
        log.info("Sending Email to {}: [{}] {}", to, subject, body);
        // Real implementation would use spring-boot-starter-mail
    }
}
