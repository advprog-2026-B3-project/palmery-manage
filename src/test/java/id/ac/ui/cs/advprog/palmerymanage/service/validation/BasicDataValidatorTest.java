package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicDataValidatorTest {

    private BasicDataValidator validator;
    private UUID workerId;
    private HarvestRequestDto request;

    @BeforeEach
    void setUp() {
        validator = new BasicDataValidator();
        workerId = UUID.randomUUID();
        request = new HarvestRequestDto();
        request.setPlantationId(UUID.randomUUID());
        request.setMandorId(UUID.randomUUID());
        request.setHarvestDate(LocalDate.now());
        request.setKgHarvested(100f);
        request.setNotes("Valid notes");
    }

    @Test
    void validate_ValidRequest_DoesNotThrow() {
        assertDoesNotThrow(() -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullPlantationId_ThrowsException() {
        request.setPlantationId(null);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullMandorId_ThrowsException() {
        request.setMandorId(null);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullHarvestDate_ThrowsException() {
        request.setHarvestDate(null);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullKgHarvested_ThrowsException() {
        request.setKgHarvested(null);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_ZeroKgHarvested_ThrowsException() {
        request.setKgHarvested(0f);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_NullNotes_ThrowsException() {
        request.setNotes(null);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }

    @Test
    void validate_EmptyNotes_ThrowsException() {
        request.setNotes("   ");
        assertThrows(IllegalArgumentException.class, () -> validator.validate(workerId, request));
    }
}
