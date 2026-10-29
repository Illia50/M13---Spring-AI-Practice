package com.accenture.springai_bootcamp_demo.controller;

import com.accenture.springai_bootcamp_demo.dto.TicketAnalysis;
import com.accenture.springai_bootcamp_demo.dto.TicketAnalysisRequest;
import com.accenture.springai_bootcamp_demo.service.TicketAnalysisService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@AllArgsConstructor
public class TicketController {

    private final TicketAnalysisService ticketAnalysisService;

    @PostMapping("/analyze")
    public TicketAnalysis analyze(@Valid @RequestBody TicketAnalysisRequest request) {
        return ticketAnalysisService.analyze(request.text());
    }
}
