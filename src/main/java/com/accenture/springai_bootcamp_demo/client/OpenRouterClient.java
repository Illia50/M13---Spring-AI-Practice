package com.accenture.springai_bootcamp_demo.client;

import com.accenture.springai_bootcamp_demo.config.OpenRouterProperties;
import com.accenture.springai_bootcamp_demo.entity.ChatMessage;
import com.accenture.springai_bootcamp_demo.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin client over the OpenRouter chat completions API, backed by Spring AI's
 * {@link ChatClient}. Keeps the public surface intentionally small: callers
 * hand over the conversation history and receive the assistant's reply text.
 */
@Slf4j
@Component
public class OpenRouterClient {
    private final ChatClient chatClient;

    public OpenRouterClient(ChatClient.Builder builder, OpenRouterProperties properties) {
        this.chatClient = builder
                .defaultSystem(properties.systemPrompt())
                .build();
    }
    public String complete(List<ChatMessage> history) {
        String reply = call(history);
        return extractContent(reply);
    }

    private String call(List<ChatMessage> history) {
        try {
            List<Message> messages = toSpringAiMessages(history);
            return chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();
        } catch (OpenRouterException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("OpenRouter request failed", ex);
            throw new OpenRouterException("Failed to reach OpenRouter: " + ex.getMessage(), ex);
        }
    }

    private List <Message> toSpringAiMessages(List<ChatMessage> history){
        List <Message> messages = new ArrayList<>();

        for (ChatMessage chatMessage : history){
            if (chatMessage.getRole() == Role.USER) {
                messages.add (new UserMessage(chatMessage.getContent()));
            } else if (chatMessage.getRole() == Role.ASSISTANT) {
                messages.add(new AssistantMessage(chatMessage.getContent()));
            } else {
                throw new IllegalArgumentException("Such a role does not exist: " + chatMessage.getRole());
            }
        }

        return messages;
    }

    private String extractContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new OpenRouterException("OpenRouter returned an empty response");
        }
        return content.trim();
    }
}
