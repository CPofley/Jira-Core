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
        // Query both open and closed PRs for this task ID branch
        String searchQuery = String.format("repo:%s/%s type:pr TASK-%d", repoOwner, repoName, taskId);
        String url = "https://api.github.com/search/issues?q=" + searchQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        // Disable aggressive caching on response
        headers.set("Cache-Control", "no-cache, no-store, must-revalidate");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        List<Map<String, Object>> result = new ArrayList<>();

        try {
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

                        String state = (String) item.get("state"); // "open" or "closed"
                        prMap.put("state", state);

                        // Check pull_request.merged_at OR state_reason/pull_request object
                        boolean isMerged = false;
                        if (item.get("pull_request") instanceof Map<?, ?> prDetails) {
                            if (prDetails.get("merged_at") != null) {
                                isMerged = true;
                            }
                        }

                        // Fallback check: If closed and state_reason is completed or pull_request has merged_at
                        if ("closed".equalsIgnoreCase(state) && (isMerged || item.get("pull_request") != null)) {
                            // Fetch specific PR details directly to ensure accurate merged status if search API omitted it
                            Integer prNumber = (Integer) item.get("number");
                            isMerged = fetchIsMergedDirectly(prNumber);
                        }

                        prMap.put("isMerged", isMerged);
                        result.add(prMap);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching PRs from GitHub: " + e.getMessage());
        }

        return CompletableFuture.completedFuture(result);
    }

    private boolean fetchIsMergedDirectly(Integer prNumber) {
        try {
            String prUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%d", repoOwner, repoName, prNumber);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    prUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> pr = response.getBody();
            if (pr != null) {
                Boolean merged = (Boolean) pr.get("merged");
                return Boolean.TRUE.equals(merged) || pr.get("merged_at") != null;
            }
        } catch (Exception e) {
            // Ignore fallback error
        }
        return false;
    }
}