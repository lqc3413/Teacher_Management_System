package com.teacher.management.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teacher.management.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public ChatServiceImpl(
            ObjectMapper objectMapper,
            @Value("${ai.dashscope.api-key:}") String configuredApiKey,
            @Value("${ai.dashscope.base-url}") String baseUrl,
            @Value("${ai.dashscope.model}") String model) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.apiKey = resolveApiKey(configuredApiKey);
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.model = model;
    }

    @Override
    public String chat(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("message 不能为空");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未读取到环境变量 ali_api_key，请先配置后重启后端");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是一名专业、友好、简洁的中文智能助手。"),
                            Map.of("role", "user", "content", message.trim())
                    ),
                    "temperature", 0.7
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("调用大模型失败，HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new IllegalStateException("大模型返回内容为空: " + response.body());
            }
            return contentNode.asText();
        } catch (IOException e) {
            throw new IllegalStateException("调用大模型时读取响应失败: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("调用大模型被中断", e);
        }
    }

    private String resolveApiKey(String configuredApiKey) {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            return configuredApiKey;
        }
        String lowerCaseKey = System.getenv("ali_api_key");
        if (lowerCaseKey != null && !lowerCaseKey.isBlank()) {
            return lowerCaseKey;
        }
        return System.getenv("ALI_API_KEY");
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
