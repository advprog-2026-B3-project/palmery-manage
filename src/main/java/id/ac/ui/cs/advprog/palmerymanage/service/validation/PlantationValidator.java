package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.service.PlantationValidationService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(2)
public class PlantationValidator implements HarvestValidator {
    
    private final PlantationValidationService plantationValidationService;

    public PlantationValidator(PlantationValidationService plantationValidationService) {
        this.plantationValidationService = plantationValidationService;
    }

    @Override
    public void validate(UUID workerId, HarvestRequestDto request) {
        if (request.getPlantationId() != null) {
            plantationValidationService.validateAndCachePlantation(request.getPlantationId());
        }
    }
}
