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

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void getActiveAlerts_returns200AndListPayload() throws Exception {
        AlertDetailDTO first = new AlertDetailDTO();
        first.setAlertId(11L);
        first.setStatus("OPEN");
        first.setSeverityScore(42);
        first.setSeverityLevel("MEDIUM");

        AlertDetailDTO second = new AlertDetailDTO();
        second.setAlertId(12L);
        second.setStatus("ACKNOWLEDGED");
        second.setSeverityScore(75);
        second.setSeverityLevel("HIGH");

        when(alertService.getActiveAlerts()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alertId").value(11))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[1].alertId").value(12))
                .andExpect(jsonPath("$[1].severityLevel").value("HIGH"));
    }

    @Test
    void getAlertById_returns200AndAlertPayload() throws Exception {
        Long alertId = 90L;
        AlertDetailDTO dto = new AlertDetailDTO();
        dto.setAlertId(alertId);
        dto.setStatus("INVESTIGATING");
        dto.setSeverityScore(93);
        dto.setSeverityLevel("CRITICAL");

        when(alertService.getAlertDetail(alertId)).thenReturn(dto);

        mockMvc.perform(get("/api/alerts/{id}", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(90))
                .andExpect(jsonPath("$.status").value("INVESTIGATING"))
                .andExpect(jsonPath("$.severityLevel").value("CRITICAL"));
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

            @Test
            void updateStatus_whenRequestIsInvalid_returns400FromExceptionHandler() throws Exception {
            Long alertId = 30L;
            AlertStatusUpdateRequest request = new AlertStatusUpdateRequest("", "");

            when(alertService.updateAlertStatus(eq(alertId), any(AlertStatusUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("status is required"));

            mockMvc.perform(put("/api/alerts/{id}/status", alertId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("status is required"));
            }

            @Test
            void getAlertById_whenAlertMissing_returns404FromExceptionHandler() throws Exception {
            Long missingAlertId = 999L;
            when(alertService.getAlertDetail(missingAlertId))
                .thenThrow(new NoSuchElementException("Alert not found: 999"));

            mockMvc.perform(get("/api/alerts/{id}", missingAlertId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Alert not found: 999"));
            }
}
