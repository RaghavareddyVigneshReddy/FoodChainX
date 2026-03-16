package com.cts.foodchainx.controller;

import com.cts.foodchainx.enums.AuditStatus;
import com.cts.foodchainx.model.Audit;
import com.cts.foodchainx.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditService auditService;

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /api/compliance/audits - Success for REGULATOR")
    @WithMockUser(roles = "REGULATOR")
    void createAudit_Success() throws Exception {
        Audit inputAudit = new Audit();
        inputAudit.setScope("Quarterly Safety Review");

        Audit savedAudit = new Audit();
        savedAudit.setAuditId(1L);
        savedAudit.setScope("Quarterly Safety Review");
        savedAudit.setStatus(AuditStatus.OPEN);

        when(auditService.createAudit(any(Audit.class))).thenReturn(savedAudit);

        mockMvc.perform(post("/api/compliance/audits")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputAudit)))
                .andExpect(status().isOk()) // Or .isCreated() if you add @ResponseStatus
                .andExpect(jsonPath("$.auditId").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("PUT /api/compliance/audits/{id}/close - Success for REGULATOR")
    @WithMockUser(roles = "REGULATOR")
    void closeAudit_Success() throws Exception {
        Audit closedAudit = new Audit();
        closedAudit.setAuditId(1L);
        closedAudit.setStatus(AuditStatus.CLOSED);

        when(auditService.closeAudit(1L)).thenReturn(closedAudit);

        mockMvc.perform(put("/api/compliance/audits/1/close")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /api/compliance/audits - Forbidden for FARMER")
    @WithMockUser(roles = "FARMER")
    void createAudit_Forbidden() throws Exception {
        Audit audit = new Audit();
        
        mockMvc.perform(post("/api/compliance/audits")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(audit)))
                .andExpect(status().isForbidden());
    }
}