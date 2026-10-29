package com.accenture.springai_bootcamp_demo.service;

import com.accenture.springai_bootcamp_demo.client.OpenRouterException;
import com.accenture.springai_bootcamp_demo.dto.TicketAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class TicketAnalysisService {

    static final int MAX_ATTEMPTS = 3;

    private static final String SYSTEM_PROMPT = """
            You are a customer support triage assistant for an online shop.
            Analyse the customer's ticket and respond ONLY with the requested JSON.
            category: one of BILLING, TECHNICAL, ACCOUNT, DELIVERY, OTHER.
            priority: one of LOW, MEDIUM, HIGH, URGENT. Use URGENT only for outages,
            security issues or money charged incorrectly right now.
            summary: one short sentence in English describing the problem.
            suggestedReply: a polite reply to the customer, at most three sentences,
            written in the same language as the ticket.
            """;

    private final ChatClient chatClient;

    public TicketAnalysisService(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public TicketAnalysis analyze(String ticketText) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                TicketAnalysis analysis = askModel(ticketText);
                if (isValid(analysis)) {
                    return analysis;
                }
                log.warn("Attempt {} returned an incomplete analysis: {}", attempt, analysis);
            } catch (RuntimeException ex) {
                lastError = ex;
                log.warn("Attempt {} failed: {}", attempt, ex.getMessage());
            }
        }
        throw new OpenRouterException(
                "Model did not return a valid ticket analysis after " + MAX_ATTEMPTS + " attempts", lastError);
    }

    private TicketAnalysis askModel(String ticketText) {
        return chatClient.prompt()
                .user(ticketText)
                .call()
                .entity(TicketAnalysis.class);
    }

    private boolean isValid(TicketAnalysis analysis) {
        return analysis != null
                && analysis.category() != null
                && analysis.priority() != null
                && StringUtils.hasText(analysis.summary())
                && StringUtils.hasText(analysis.suggestedReply());
    }
}
