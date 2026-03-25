package az.hkbank.module.ai.service;

import az.hkbank.common.exception.BankException;
import az.hkbank.common.exception.ErrorCode;
import az.hkbank.config.ai.GeminiProperties;
import az.hkbank.module.ai.config.SystemPromptConfig;
import az.hkbank.module.ai.entity.ChatMessage;
import az.hkbank.module.ai.entity.MessageRole;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for interacting with Google Gemini API.
 * Handles API communication and response parsing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiApiService {

    private final GeminiProperties geminiProperties;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();

    /**
     * Sends a message to Gemini API with conversation history.
     *
     * @param history the conversation history (last 10 messages)
     * @param userMessage the new user message
     * @return AI assistant response
     */
    public String sendMessage(List<ChatMessage> history, String userMessage) {
        log.info("Sending message to Gemini API");

        try {
            String requestBody = buildRequestBody(history, userMessage);
            String url = geminiProperties.getApiUrl() + "?key=" + geminiProperties.getApiKey();
            
            log.debug("Gemini API URL: {}", geminiProperties.getApiUrl());
            log.debug("Request body length: {} characters", requestBody.length());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Gemini API response received successfully");
                return parseResponse(response.getBody());
            } else {
                log.error("Gemini API returned non-OK status: {}, body: {}", 
                        response.getStatusCode(), response.getBody());
                throw new BankException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI service returned error status");
            }

        } catch (BankException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to communicate with Gemini API: {}", e.getMessage(), e);
            throw new BankException(ErrorCode.AI_SERVICE_UNAVAILABLE, "AI service is temporarily unavailable");
        }
    }

    private String buildRequestBody(List<ChatMessage> history, String userMessage) {
        JsonObject request = new JsonObject();
        JsonArray contents = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "user");
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", SystemPromptConfig.SYSTEM_PROMPT);
        systemParts.add(systemPart);
        systemMessage.add("parts", systemParts);
        contents.add(systemMessage);

        JsonObject systemResponse = new JsonObject();
        systemResponse.addProperty("role", "model");
        JsonArray systemResponseParts = new JsonArray();
        JsonObject systemResponsePart = new JsonObject();
        systemResponsePart.addProperty("text", SystemPromptConfig.SYSTEM_ACKNOWLEDGMENT);
        systemResponseParts.add(systemResponsePart);
        systemResponse.add("parts", systemResponseParts);
        contents.add(systemResponse);

        List<ChatMessage> reversedHistory = new ArrayList<>(history);
        Collections.reverse(reversedHistory);

        for (ChatMessage msg : reversedHistory) {
            JsonObject message = new JsonObject();
            message.addProperty("role", msg.getRole() == MessageRole.USER ? "user" : "model");
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", msg.getContent());
            parts.add(part);
            message.add("parts", parts);
            contents.add(message);
        }

        JsonObject newMessage = new JsonObject();
        newMessage.addProperty("role", "user");
        JsonArray newParts = new JsonArray();
        JsonObject newPart = new JsonObject();
        newPart.addProperty("text", userMessage);
        newParts.add(newPart);
        newMessage.add("parts", newParts);
        contents.add(newMessage);

        request.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("maxOutputTokens", geminiProperties.getMaxTokens());
        generationConfig.addProperty("temperature", geminiProperties.getTemperature());
        request.add("generationConfig", generationConfig);

        return gson.toJson(request);
    }

    private String parseResponse(String responseBody) {
        try {
            log.debug("Parsing Gemini API response, length: {} characters", responseBody.length());
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");

                if (parts.size() > 0) {
                    JsonObject part = parts.get(0).getAsJsonObject();
                    String text = part.get("text").getAsString();
                    log.debug("Successfully extracted text from Gemini response");
                    return text;
                }
            }

            log.error("Unexpected Gemini API response format. Response: {}", responseBody);
            throw new BankException(ErrorCode.AI_SERVICE_UNAVAILABLE, "Invalid AI response format");

        } catch (BankException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini API response: {}", e.getMessage(), e);
            log.error("Response body: {}", responseBody);
            throw new BankException(ErrorCode.AI_SERVICE_UNAVAILABLE, "Failed to parse AI response");
        }
    }
}
