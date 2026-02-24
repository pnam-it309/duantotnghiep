package com.example.be.service;

import com.example.be.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to specific user
     */
    public void notifyUser(Long userId, NotificationDTO notification) {
        messagingTemplate.convertAndSend(
                "/queue/notifications/" + userId,
                notification);
    }

    /**
     * Broadcast to all connected users
     */
    public void broadcastToAll(NotificationDTO notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification);
    }

    /**
     * Send to specific topic
     */
    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }
}
