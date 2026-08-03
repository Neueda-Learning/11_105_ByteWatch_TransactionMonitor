package org.ByteWatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ByteWatch.model.AlertDetailDTO;
import org.ByteWatch.model.AlertStatusUpdateRequest;
import org.ByteWatch.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoint-level verification for alert lifecycle transition behavior.
 */
class AlertControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = mock(AlertService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new AlertController(alertService)).build();
    }

    @Test
    void updateStatus_returns200AndUpdatedAlertDetail() throws Exception {
        Long alertId = 77L;
        AlertStatusUpdateRequest request = new AlertStatusUpdateRequest("DISMISSED", "Verified with customer");

        AlertDetailDTO responseDto = new AlertDetailDTO();
        responseDto.setAlertId(alertId);
        responseDto.setStatus("DISMISSED");
        responseDto.setSeverityScore(70);
        responseDto.setSeverityLevel("HIGH");

        when(alertService.updateAlertStatus(eq(alertId), any(AlertStatusUpdateRequest.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/alerts/{id}/status", alertId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(77))
                .andExpect(jsonPath("$.status").value("DISMISSED"))
                .andExpect(jsonPath("$.severityLevel").value("HIGH"));
    }
}
