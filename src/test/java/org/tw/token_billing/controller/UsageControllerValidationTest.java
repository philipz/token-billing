package org.tw.token_billing.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tw.token_billing.service.UsageService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UsageController validation pipeline.
 * Tests per SRS-F-6 validation order.
 */
@WebMvcTest(UsageController.class)
class UsageControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsageService usageService;

    @Test
    void shouldReturnBadRequestForMissingCustomerId() throws Exception {
        String requestBody = """
            {
                "promptTokens": 30000,
                "completionTokens": 20000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForMissingPromptTokens() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001",
                "completionTokens": 20000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForMissingCompletionTokens() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001",
                "promptTokens": 30000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForInvalidCustomerIdFormat() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST@001!",
                "promptTokens": 30000,
                "completionTokens": 20000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForNegativePromptTokens() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001",
                "promptTokens": -1000,
                "completionTokens": 20000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForNegativeCompletionTokens() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001",
                "promptTokens": 30000,
                "completionTokens": -5000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForExceedingMaxTokenLimit() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001",
                "promptTokens": 3000000000,
                "completionTokens": 20000
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForUnknownProperties() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001",
                "promptTokens": 30000,
                "completionTokens": 20000,
                "unknownField": "should-fail"
            }
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void shouldReturnBadRequestForEmptyRequestBody() throws Exception {
        String requestBody = "{}";

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestForInvalidJson() throws Exception {
        String requestBody = """
            {
                "customerId": "CUST-001
            """;

        mockMvc.perform(post("/api/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }
}