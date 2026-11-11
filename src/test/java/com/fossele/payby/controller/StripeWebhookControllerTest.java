package com.fossele.payby.controller;

import com.fossele.payby.service.AsynchronousWebhookProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StripeWebhookController.class)
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsynchronousWebhookProcessor asyncProcessor;

    @Test
    void testWebhookEndpointInvalidSignatureReturnsBadRequest() throws Exception {
        String payload = "{\"id\":\"evt_123\",\"object\":\"event\"}";

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=123,v1=fake_signature_hash")
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}