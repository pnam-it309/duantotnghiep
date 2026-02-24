package com.example.be.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseService {

    public void sendSMS(String phoneNumber, String message) {
        // [MOCK] Integration with Firebase/Twilio for SMS
        log.info("Sending SMS to {}: {}", phoneNumber, message);
        // Real implementation would use Firebase Admin SDK or a provider like Twilio
    }
}
