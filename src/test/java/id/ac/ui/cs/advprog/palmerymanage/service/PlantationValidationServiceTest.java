package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantationValidationServiceTest {

    @Mock
    private PlantationService plantationService;

    @InjectMocks
    private PlantationValidationService plantationValidationService;

    private UUID plantationId;

    @BeforeEach
    void setUp() {
        plantationId = UUID.randomUUID();
    }

    @Test
    void validateAndCachePlantation_found_returnsTrue() {
        Plantation plantation = new Plantation();
        plantation.setId(plantationId);
        when(plantationService.getPlantationById(plantationId)).thenReturn(null);

        boolean result = plantationValidationService.validateAndCachePlantation(plantationId);

        assertTrue(result);
        verify(plantationService).getPlantationById(plantationId);
    }

    @Test
    void validateAndCachePlantation_notFound_throwsException() {
        when(plantationService.getPlantationById(plantationId)).thenThrow(new RuntimeException("Not found"));

        assertThrows(IllegalArgumentException.class,
                () -> plantationValidationService.validateAndCachePlantation(plantationId));
    }

    @Test
    void evictPlantationCache_doesNotThrow() {
        assertDoesNotThrow(() -> plantationValidationService.evictPlantationCache(plantationId));
    }

    @Test
    void evictAllPlantationCache_doesNotThrow() {
        assertDoesNotThrow(() -> plantationValidationService.evictAllPlantationCache());
    }
}
