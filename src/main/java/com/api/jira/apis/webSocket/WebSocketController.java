package com.api.jira.apis.webSocket;


import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/tasks/{taskId}/lock")
    public void handleTaskFieldLock(@DestinationVariable Integer taskId, @Payload Map<String, Object> lockPayload) {
        // Broadcast lock status to all clients watching this task topic
        messagingTemplate.convertAndSend("/topic/tasks/" + taskId, lockPayload);
    }
}
