package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestSummaryDto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestProviderServiceImplTest {

    @Mock
    private HarvestResultRepository harvestResultRepository;

    @InjectMocks
    private HarvestProviderServiceImpl providerService;

    private UUID harvestId;
    private UUID workerId;
    private UUID plantationId;
    private HarvestResult harvestResult;

    @BeforeEach
    void setUp() {
        harvestId = UUID.randomUUID();
        workerId = UUID.randomUUID();
        plantationId = UUID.randomUUID();

        harvestResult = HarvestResult.builder()
                .id(harvestId)
                .workerId(workerId)
                .plantation(id.ac.ui.cs.advprog.palmerymanage.model.Plantation.builder().id(plantationId).build())
                .harvestDate(LocalDate.now())
                .kgHarvested(150.0f)
                .status("APPROVED")
                .readyForDelivery(true)
                .build();
    }

    @Test
    void testIsReadyForDelivery_True() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(harvestResult));

        boolean isReady = providerService.isReadyForDelivery(harvestId);
        
        assertTrue(isReady);
    }

    @Test
    void testIsReadyForDelivery_NotFound_ThrowsException() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.isReadyForDelivery(harvestId);
        });

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void testGetHarvestStatus() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(harvestResult));

        String status = providerService.getHarvestStatus(harvestId);
        
        assertEquals("APPROVED", status);
    }

    @Test
    void testGetHarvestStatus_NotFound_ThrowsException() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            providerService.getHarvestStatus(harvestId);
        });

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void testFindHarvestSummary_Found() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(harvestResult));

        Optional<HarvestSummaryDto> summary = providerService.findHarvestSummary(harvestId);
        
        assertTrue(summary.isPresent());
        assertEquals(harvestId, summary.get().getHarvestId());
        assertEquals(workerId, summary.get().getWorkerId());
    }

    @Test
    void testFindHarvestSummary_NotFound() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.empty());

        Optional<HarvestSummaryDto> summary = providerService.findHarvestSummary(harvestId);
        
        assertFalse(summary.isPresent());
    }

    @Test
    void testGetApprovedHarvestsByWorker() {
        when(harvestResultRepository.findByWorkerId(workerId)).thenReturn(List.of(harvestResult));

        List<HarvestSummaryDto> summaries = providerService.getApprovedHarvestsByWorker(workerId);
        
        assertEquals(1, summaries.size());
        assertEquals(harvestId, summaries.get(0).getHarvestId());
    }

    @Test
    void testGetApprovedHarvestsByPlantation() {
        when(harvestResultRepository.findByPlantation_Id(plantationId)).thenReturn(List.of(harvestResult));

        List<HarvestSummaryDto> summaries = providerService.getApprovedHarvestsByPlantation(plantationId);
        
        assertEquals(1, summaries.size());
        assertEquals(harvestId, summaries.get(0).getHarvestId());
    }
}
