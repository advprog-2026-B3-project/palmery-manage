package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import java.util.UUID;

public interface HarvestValidator {
    void validate(UUID workerId, HarvestRequestDto request);
}
