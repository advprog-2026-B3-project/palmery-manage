package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuplicateHarvestValidatorTest {

    @Mock
    private HarvestResultRepository harvestResultRepository;

    @InjectMocks
    private DuplicateHarvestValidator validator;

    private UUID workerId;
    private HarvestRequestDto request;

    @BeforeEach
    void setUp() {
        workerId = UUID.randomUUID();
        request = new HarvestRequestDto();
        request.setHarvestDate(LocalDate.now());
    }

    @Test
    void validate_NoDuplicate_DoesNotThrow() {
        when(harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, request.getHarvestDate()))
                .thenReturn(false);
        
        assertDoesNotThrow(() -> validator.validate(workerId, request));
    }

    @Test
    void validate_DuplicateExists_ThrowsException() {
        when(harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, request.getHarvestDate()))
                .thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullHarvestDate_DoesNotCallRepository() {
        request.setHarvestDate(null);
        assertDoesNotThrow(() -> validator.validate(workerId, request));
        verify(harvestResultRepository, never()).existsByWorkerIdAndHarvestDate(any(), any());
    }
}
