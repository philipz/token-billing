package org.tw.token_billing.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tw.token_billing.dto.UsageRequest;
import org.tw.token_billing.dto.UsageResponse;
import org.tw.token_billing.service.UsageService;

/**
 * REST controller for token usage billing endpoint.
 * Follows SRS-F-1 endpoint specification.
 */
@RestController
@RequestMapping("/api")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    /**
     * POST /api/usage - Process token usage and create bill.
     * @param request the usage request
     * @return usage response with billing details
     */
    @PostMapping("/usage")
    public ResponseEntity<UsageResponse> createUsage(@Valid @RequestBody UsageRequest request) {
        UsageResponse response = usageService.processUsage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}