package com.api.jira.apis.webSocket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class GithubService {

    @Value("${github.repo.owner}")
    private String repoOwner;

    @Value("${github.repo.name}")
    private String repoName;

    private final RestTemplate restTemplate;

    public GithubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async("customExecutor")
    public CompletableFuture<List<Map<String, Object>>> getPrForATaskId(Integer taskId) {
        String searchQuery = String.format("repo:%s/%s type:pr head:TASK-%d", repoOwner, repoName, taskId);
        String url = "https://api.github.com/search/issues?q=" + searchQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // Using ParameterizedTypeReference eliminates raw type warnings
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.get("items") instanceof List<?> rawList) {
                for (Object obj : rawList) {
                    if (obj instanceof Map<?, ?> item) {
                        Map<String, Object> prMap = new HashMap<>();
                        prMap.put("id", item.get("id"));
                        prMap.put("number", item.get("number"));
                        prMap.put("title", item.get("title"));
                        prMap.put("url", item.get("html_url"));
                        prMap.put("state", item.get("state"));

                        boolean isMerged = item.containsKey("pull_request")
                                && item.get("pull_request") instanceof Map<?, ?> prDetails
                                && prDetails.get("merged_at") != null;

                        prMap.put("isMerged", isMerged);
                        result.add(prMap);
                    }
                }
            }
        } catch (Exception e) {
            // Silently fall back to empty list on error
            throw e;
        }

        return CompletableFuture.completedFuture(result);
    }
}
