package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.service.PlantationValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantationValidatorTest {

    @Mock
    private PlantationValidationService plantationValidationService;

    @InjectMocks
    private PlantationValidator validator;

    private UUID workerId;
    private HarvestRequestDto request;

    @BeforeEach
    void setUp() {
        workerId = UUID.randomUUID();
        request = new HarvestRequestDto();
        request.setPlantationId(UUID.randomUUID());
    }

    @Test
    void validate_ValidPlantation_DoesNotThrow() {
        when(plantationValidationService.validateAndCachePlantation(request.getPlantationId())).thenReturn(true);
        assertDoesNotThrow(() -> validator.validate(workerId, request));
        verify(plantationValidationService).validateAndCachePlantation(request.getPlantationId());
    }

    @Test
    void validate_InvalidPlantation_ThrowsException() {
        when(plantationValidationService.validateAndCachePlantation(request.getPlantationId()))
                .thenThrow(new IllegalArgumentException("Kebun tidak valid"));
        
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullPlantationId_DoesNotCallService() {
        request.setPlantationId(null);
        assertDoesNotThrow(() -> validator.validate(workerId, request));
        verify(plantationValidationService, never()).validateAndCachePlantation(any());
    }
}
