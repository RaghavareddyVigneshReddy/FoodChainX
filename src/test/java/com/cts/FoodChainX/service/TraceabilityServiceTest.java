// package com.cts.FoodChainX.service;

// import static org.mockito.Mockito.*;
// import static org.junit.jupiter.api.Assertions.*;

// import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponse;
// import com.cts.FoodChainX.model.*;
// import com.cts.FoodChainX.repository.TraceRecordRepository;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.Optional;

// @ExtendWith(MockitoExtension.class)
// class TraceabilityServiceTest {

//     @Mock
//     private TraceRecordRepository traceRecordRepository;

//     @InjectMocks
//     private TraceabilityService traceabilityService;

//     @Test
//     void testGetTraceRecord_Success() {
//         // 1. Arrange: Setup your mock objects
//         ProductionBatch batch = ProductionBatch.builder().productionId(1).build();
//         TraceRecord record = new TraceRecord();
//         record.setProductionBatch(batch);
//         record.setStatus("SHIPPED");

//         when(traceRecordRepository.findByProductionBatch_ProductionId(1))
//             .thenReturn(Optional.of(record));

//         // 2. Act: Call the method you're testing
//         TraceRecordResponse response = traceabilityService.getTraceabilityData(1);

//         // 3. Assert: Verify the result
//         assertEquals("SHIPPED", response.status());
//         verify(traceRecordRepository, times(1)).findByProductionBatch_ProductionId(1);
//     }
// }