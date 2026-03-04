package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PanenApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Harvest;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PanenApprovedListener {

    private final HarvestRepository harvestRepository;

    public PanenApprovedListener(HarvestRepository harvestRepository) {
        this.harvestRepository = harvestRepository;
    }

    @EventListener
    @Transactional
    public void onPanenApproved(PanenApprovedEvent event) {
        Harvest harvest = harvestRepository.findById(event.panenId()).orElseGet(() -> {
            Harvest h = new Harvest();
            h.setId(event.panenId());
            h.setMandorId(event.mandorId());
            h.setKebunId(event.kebunId());
            h.setBeratKg(event.beratKg());
            return h;
        });

        if (!harvest.isReadyForDelivery()) {
            harvest.setReadyForDelivery(true);
            harvest.setStatus("SIAP_ANGKUT");
        }

        harvestRepository.save(harvest);
    }
}

