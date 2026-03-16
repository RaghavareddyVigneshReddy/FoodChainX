package com.cts.foodchainx.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cts.foodchainx.dto.quality.QualityRequestDto;
import com.cts.foodchainx.dto.quality.QualityResponseDto;
import com.cts.foodchainx.enums.QualityStatus;
import com.cts.foodchainx.service.ProductionBatchService;
import com.cts.foodchainx.service.QualityCheckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
class QualityCheckControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QualityCheckService qualityCheckService;

    @Mock
    private ProductionBatchService productionBatchService;

    @InjectMocks
    private QualityCheckController qualityCheckController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(qualityCheckController).build();
        
        // Registering JavaTimeModule to handle LocalDate in QualityResponseDto
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void performInspection_ShouldReturnSuccessMessage() throws Exception {
        QualityRequestDto requestDto = new QualityRequestDto();
        requestDto.setBatchId(101L);
        requestDto.setStatus(QualityStatus.PASSED); // Changed APPROVED -> PASSED
        
        String successMsg = "Inspection completed and batch status updated to PASSED";
        
        when(productionBatchService.performQualityCheck(any(QualityRequestDto.class))).thenReturn(successMsg);

        mockMvc.perform(post("/api/quality-checks/inspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(successMsg));
    }

    @Test
    void getInspectionsByStatus_ShouldReturnList() throws Exception {
        QualityResponseDto responseDto = new QualityResponseDto();
        responseDto.setQualityId(50L);
        responseDto.setStatus(QualityStatus.PASSED); // Changed APPROVED -> PASSED
        responseDto.setFindings("All parameters normal");
        
        when(qualityCheckService.getInspectionsByStatus(QualityStatus.PASSED)) // Changed APPROVED -> PASSED
            .thenReturn(List.of(responseDto));

        // Testing enum path variable conversion with PASSED
        mockMvc.perform(get("/api/quality-checks/status/PASSED"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].qualityId").value(50L))
            .andExpect(jsonPath("$[0].findings").value("All parameters normal"));
    }

    @Test
    void deleteQualityLog_ShouldReturnConfirmation() throws Exception {
        String msg = "Quality log removed and batch reset to PENDING";
        
        when(qualityCheckService.removeQualityLog(anyLong())).thenReturn(msg);

        mockMvc.perform(delete("/api/quality-checks/50"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(msg));
    }
}
