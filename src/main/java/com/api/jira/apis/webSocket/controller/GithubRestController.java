package com.api.jira.apis.webSocket.controller;

import com.api.jira.apis.webSocket.service.GithubService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/github")
public class GithubRestController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GithubService githubService;

    public GithubRestController(SimpMessagingTemplate messagingTemplate, GithubService githubService) {
        this.messagingTemplate = messagingTemplate;
        this.githubService = githubService;
    }

    // Handles direct manual fetch from frontend (fixes 'No static resource api/github/pr/{taskId}')
    @GetMapping("/prs/{taskId}")
    public ResponseEntity<List<Map<String, Object>>> getPullRequestsForTask(@PathVariable Integer taskId) {
        try {
            List<Map<String, Object>> pullRequests = githubService.getPrForATaskId(taskId).get();
            return ResponseEntity.ok(pullRequests);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Handles GitHub Webhook POST event
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleGithubWebhook(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("pull_request") && payload.get("pull_request") instanceof Map<?, ?> pr) {

            String title = (String) pr.get("title");
            Map<?, ?> head = (Map<?, ?>) pr.get("head");
            String branchName = head != null ? (String) head.get("ref") : "";

            Integer taskId = extractTaskId(title, branchName);

            if (taskId != null) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}

                    githubService.getPrForATaskId(taskId).thenAccept(prList -> {
                        Map<String, Object> wsPayload = new HashMap<>();
                        wsPayload.put("type", "PR_FETCH_COMPLETE");
                        wsPayload.put("pullRequests", prList);

                        messagingTemplate.convertAndSend("/topic/tasks/" + taskId, wsPayload);
                    });
                });
            }
        }
        return ResponseEntity.ok().build();
    }

    private Integer extractTaskId(String title, String branchName) {
        String combined = (title + " " + branchName).toUpperCase();
        Pattern pattern = Pattern.compile("TASK-(\\d+)");
        Matcher matcher = pattern.matcher(combined);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}