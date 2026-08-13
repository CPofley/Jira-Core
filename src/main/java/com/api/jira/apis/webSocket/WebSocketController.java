package com.api.jira.apis.webSocket;


import com.api.jira.apis.webSocket.service.GithubService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final GithubService githubService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, GithubService githubService) {
        this.messagingTemplate = messagingTemplate;
        this.githubService = githubService;
    }

    @MessageMapping("/tasks/{taskId}/lock")
    public void handleTaskFieldLock(@DestinationVariable Integer taskId, @Payload Map<String, Object> lockPayload) {
        // Broadcast lock status to all clients watching this task topic
        messagingTemplate.convertAndSend("/topic/tasks/" + taskId, lockPayload);
    }

    @MessageMapping("/tasks/{taskId}/fetch-prs")
    public void handleFetchPullRequests(@DestinationVariable Integer taskId) {
        githubService.getPrForATaskId(taskId)
                .thenAccept(pullRequests -> {
                    Map<String, Object> wsPayload = Map.of(
                            "type", "PR_FETCH_COMPLETE",
                            "pullRequests", pullRequests
                    );
                    messagingTemplate.convertAndSend("/topic/tasks/" + taskId, wsPayload);
                });
    }
}
