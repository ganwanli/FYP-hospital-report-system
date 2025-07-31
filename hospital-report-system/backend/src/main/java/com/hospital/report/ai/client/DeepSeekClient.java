package com.hospital.report.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.ai.client.dto.ChatRequest;
import com.hospital.report.ai.client.dto.ChatResponse;
import com.hospital.report.ai.config.AIConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class DeepSeekClient {
    
    private final WebClient webClient;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    
    public DeepSeekClient(WebClient deepSeekWebClient, AIConfig aiConfig) {
        this.webClient = deepSeekWebClient;
        this.aiConfig = aiConfig;
        this.objectMapper = new ObjectMapper();
    }
    
    public Mono<ChatResponse> chat(List<ChatRequest.ChatMessage> messages) {
        ChatRequest request = buildChatRequest(messages, false);
        
        return webClient
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> 
                response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("DeepSeek API error response: {}", body);
                        return Mono.error(new RuntimeException("DeepSeek API调用失败: " + body));
                    })
            )
            .bodyToMono(ChatResponse.class)
            .timeout(Duration.ofMillis(aiConfig.getTimeout()))
            .retryWhen(Retry.backoff(aiConfig.getMaxRetries(), Duration.ofMillis(aiConfig.getRetryDelay()))
                .filter(throwable -> !(throwable instanceof IllegalArgumentException))
                .doBeforeRetry(retrySignal -> 
                    log.warn("Retrying DeepSeek API call, attempt: {}", retrySignal.totalRetries() + 1)
                )
            )
            .doOnSuccess(response -> {
                if (response != null && response.getUsage() != null && response.getUsage().getTotalTokens() != null) {
                    log.info("DeepSeek API call successful, tokens used: {}", response.getUsage().getTotalTokens());
                } else {
                    log.info("DeepSeek API call successful, no token usage information");
                }
            })
            .doOnError(error -> log.error("DeepSeek API call failed", error));
    }
    
    public Flux<String> chatStream(List<ChatRequest.ChatMessage> messages) {
        ChatRequest request = buildChatRequest(messages, true);
        
        return webClient
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> 
                response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("DeepSeek API stream error: {}", body);
                        return Mono.error(new RuntimeException("DeepSeek API流式调用失败: " + body));
                    })
            )
            .bodyToFlux(DataBuffer.class)
            .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return new String(bytes, StandardCharsets.UTF_8);
            })
            .flatMap(chunk -> {
                // Split chunk by lines in case multiple data lines come in one chunk
                String[] lines = chunk.split("\n");
                return Flux.fromArray(lines);
            })
            .filter(line -> line.startsWith("data: "))
            .map(line -> line.substring(6).trim())
            .filter(data -> !data.equals("[DONE]") && !data.isEmpty())
            .map(this::parseStreamResponse)
            .filter(content -> !content.isEmpty())
            .timeout(Duration.ofMillis(aiConfig.getTimeout()))
            .onErrorResume(throwable -> {
                log.error("Stream chat error", throwable);
                return Flux.just("抱歉，AI服务暂时不可用，请稍后重试。");
            })
            .doOnNext(content -> log.debug("Stream content received: {}", content))
            .doOnComplete(() -> log.info("Stream chat completed"))
            .doOnError(error -> log.error("Stream chat failed", error));
    }
    
    private ChatRequest buildChatRequest(List<ChatRequest.ChatMessage> messages, boolean stream) {
        ChatRequest request = new ChatRequest();
        request.setModel(aiConfig.getModel());
        request.setMessages(messages);
        request.setTemperature(aiConfig.getTemperature());
        request.setMaxTokens(aiConfig.getMaxTokens());
        request.setStream(stream);
        return request;
    }
    
    private String parseStreamResponse(String data) {
        try {
            JsonNode node = objectMapper.readTree(data);
            JsonNode choices = node.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode delta = choices.get(0).get("delta");
                if (delta != null && delta.has("content")) {
                    String content = delta.get("content").asText();
                    return content != null ? content : "";
                }
            }
        } catch (Exception e) {
            log.debug("解析流式响应失败: {}", data, e);
        }
        return "";
    }
    
    public Mono<Boolean> testConnection() {
        List<ChatRequest.ChatMessage> testMessages = List.of(
            ChatRequest.ChatMessage.user("Hello")
        );
        
        return chat(testMessages)
            .map(response -> response != null && !response.getChoices().isEmpty())
            .onErrorReturn(false)
            .doOnNext(success -> {
                if (success) {
                    log.info("DeepSeek API connection test successful");
                } else {
                    log.warn("DeepSeek API connection test failed");
                }
            });
    }
}