package com.api.jira.apis.webSocket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    Logger LOG = LoggerFactory.getLogger(GithubService.class);

    @Value("${github.repo.owner}")
    private String repoOwner;

    @Value("${github.repo.name}")
    private String repoName;

    @Value("${github.repo.ui.repo}")
    private String uiRepoName;

    @Value("${github.token}")
    private String token;

    private final RestTemplate restTemplate;

    public GithubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async("customExecutor")
    public CompletableFuture<List<Map<String, Object>>> getPrForATaskId(Integer taskId) {
        // Fetch PRs from Backend Repo and UI Repo concurrently
        CompletableFuture<List<Map<String, Object>>> backendPrs = fetchPrsForRepo(repoName, taskId);
        CompletableFuture<List<Map<String, Object>>> uiPrs = fetchPrsForRepo(uiRepoName, taskId);

        return CompletableFuture.allOf(backendPrs, uiPrs)
                .thenApply(v -> {
                    List<Map<String, Object>> combinedList = new ArrayList<>();
                    combinedList.addAll(backendPrs.join());
                    combinedList.addAll(uiPrs.join());
                    return combinedList;
                });
    }

    private CompletableFuture<List<Map<String, Object>>> fetchPrsForRepo(String targetRepo, Integer taskId) {
        LOG.info("Token: "+token);
        String searchQuery = String.format("repo:%s/%s type:pr TASK-%d", repoOwner, targetRepo, taskId);
        String url = "https://api.github.com/search/issues?q=" + searchQuery;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.set("Authorization", "Bearer "+token);

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
                        prMap.put("title", targetRepo + ": " + item.get("title")); // Optional: Prefix title to distinguish UI vs Backend PRs
                        prMap.put("url", item.get("html_url"));

                        String state = (String) item.get("state");
                        prMap.put("state", state);

                        boolean isMerged = false;
                        if (item.get("pull_request") instanceof Map<?, ?> prDetails) {
                            if (prDetails.get("merged_at") != null) {
                                isMerged = true;
                            }
                        }

                        if ("closed".equalsIgnoreCase(state) && (isMerged || item.get("pull_request") != null)) {
                            Integer prNumber = (Integer) item.get("number");
                            isMerged = fetchIsMergedDirectly(targetRepo, prNumber);
                        }

                        prMap.put("isMerged", isMerged);
                        result.add(prMap);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching PRs from GitHub repo " + targetRepo + ": " + e.getMessage());
        }

        return CompletableFuture.completedFuture(result);
    }

    private boolean fetchIsMergedDirectly(String targetRepo, Integer prNumber) {
        try {
            LOG.info("Token: "+token);
            String prUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%d", repoOwner, targetRepo, prNumber);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + token);
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