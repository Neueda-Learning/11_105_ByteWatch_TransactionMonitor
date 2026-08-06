package org.ByteWatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ByteWatch.model.SimulationStartRequest;
import org.ByteWatch.model.SimulationStatusResponse;
import org.ByteWatch.service.TransactionSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SimulationControllerIntegrationTest {

    private MockMvc mockMvc;
    private TransactionSimulationService transactionSimulationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        transactionSimulationService = mock(TransactionSimulationService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new SimulationController(transactionSimulationService)).build();
    }

    @Test
    void status_returns200AndCurrentState() throws Exception {
        SimulationStatusResponse response = new SimulationStatusResponse();
        response.setRunning(true);
        response.setGeneratedTransactions(10);
        response.setGeneratedAlerts(2);

        when(transactionSimulationService.currentStatus()).thenReturn(response);

        mockMvc.perform(get("/api/simulation/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true))
                .andExpect(jsonPath("$.generatedTransactions").value(10))
                .andExpect(jsonPath("$.generatedAlerts").value(2));
    }

    @Test
    void start_returns200AndUpdatedState() throws Exception {
        SimulationStatusResponse response = new SimulationStatusResponse();
        response.setRunning(true);
        response.setMinDelayMs(300);
        response.setMaxDelayMs(700);

        when(transactionSimulationService.start(any(SimulationStartRequest.class))).thenReturn(response);

        SimulationStartRequest request = new SimulationStartRequest();
        request.setMinDelayMs(300);
        request.setMaxDelayMs(700);

        mockMvc.perform(post("/api/simulation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true))
                .andExpect(jsonPath("$.minDelayMs").value(300))
                .andExpect(jsonPath("$.maxDelayMs").value(700));
    }

    @Test
    void stop_returns200AndStoppedState() throws Exception {
        SimulationStatusResponse response = new SimulationStatusResponse();
        response.setRunning(false);
        when(transactionSimulationService.stop()).thenReturn(response);

        mockMvc.perform(post("/api/simulation/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false));
    }

    @Test
    void start_whenServiceRejectsRequest_returns400() throws Exception {
        when(transactionSimulationService.start(any(SimulationStartRequest.class)))
                .thenThrow(new IllegalArgumentException("minDelayMs and maxDelayMs must be >= 200 and min <= max"));

        SimulationStartRequest request = new SimulationStartRequest();
        request.setMinDelayMs(100);
        request.setMaxDelayMs(50);

        mockMvc.perform(post("/api/simulation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("minDelayMs and maxDelayMs must be >= 200 and min <= max"));
    }
}
