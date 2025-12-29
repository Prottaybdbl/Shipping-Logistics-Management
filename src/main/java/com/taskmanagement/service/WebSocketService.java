package com.taskmanagement.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast a message to all users viewing a specific board
     */
    public void sendBoardUpdate(Long boardId, String action, Map<String, Object> payload) {
        String topic = "/topic/board/" + boardId;
        
        Map<String, Object> message = Map.of(
            "action", action,
            "timestamp", System.currentTimeMillis(),
            "data", payload
        );
        
        messagingTemplate.convertAndSend(topic, message);
    }

    /**
     * Send a message to a specific user
     */
    public void sendToUser(String username, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(username, destination, payload);
    }
}
