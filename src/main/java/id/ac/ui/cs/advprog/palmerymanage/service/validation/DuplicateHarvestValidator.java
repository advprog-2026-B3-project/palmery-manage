package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(3)
public class DuplicateHarvestValidator implements HarvestValidator {
    
    private final HarvestResultRepository harvestResultRepository;

    public DuplicateHarvestValidator(HarvestResultRepository harvestResultRepository) {
        this.harvestResultRepository = harvestResultRepository;
    }

    @Override
    public void validate(UUID workerId, HarvestRequestDto request) {
        if (request.getHarvestDate() != null) {
            if (harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, request.getHarvestDate())) {
                throw new IllegalArgumentException("Buruh sudah melaporkan Harvest pada tanggal ini.");
            }
        }
    }
}
