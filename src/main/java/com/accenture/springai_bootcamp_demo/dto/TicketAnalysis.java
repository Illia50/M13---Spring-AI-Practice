package com.accenture.springai_bootcamp_demo.dto;

public record TicketAnalysis(
        Category category,
        Priority priority,
        String summary,
        String suggestedReply) {

    public enum Category { BILLING, TECHNICAL, ACCOUNT, DELIVERY, OTHER }

    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
}
