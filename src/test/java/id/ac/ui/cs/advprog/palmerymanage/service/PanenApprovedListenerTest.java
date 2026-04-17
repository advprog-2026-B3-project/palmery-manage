package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PanenApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Harvest;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PanenApprovedListenerTest {

    @Mock
    private HarvestRepository harvestRepository;

    @InjectMocks
    private PanenApprovedListener listener;

    @Test
    void marksExistingHarvestReadyForDelivery() {
        Harvest existing = new Harvest();
        existing.setId("PAN-1");
        existing.setMandorId("MDR-1");
        existing.setKebunId("KEB-1");
        existing.setBeratKg(100);
        existing.setReadyForDelivery(false);

        when(harvestRepository.findById("PAN-1")).thenReturn(Optional.of(existing));

        PanenApprovedEvent event = new PanenApprovedEvent("PAN-1", "MDR-1", "KEB-1", 100, Instant.now());
        listener.onPanenApproved(event);

        ArgumentCaptor<Harvest> captor = ArgumentCaptor.forClass(Harvest.class);
        verify(harvestRepository).save(captor.capture());
        Harvest saved = captor.getValue();
        assertTrue(saved.isReadyForDelivery());
        assertEquals("SIAP_ANGKUT", saved.getStatus());
    }

    @Test
    void createsNewHarvestIfNotExists() {
        when(harvestRepository.findById("PAN-2")).thenReturn(Optional.empty());

        PanenApprovedEvent event = new PanenApprovedEvent("PAN-2", "MDR-1", "KEB-1", 150, Instant.now());
        listener.onPanenApproved(event);

        ArgumentCaptor<Harvest> captor = ArgumentCaptor.forClass(Harvest.class);
        verify(harvestRepository).save(captor.capture());
        Harvest saved = captor.getValue();
        assertEquals("PAN-2", saved.getId());
        assertEquals("MDR-1", saved.getMandorId());
        assertEquals("KEB-1", saved.getKebunId());
        assertEquals(150, saved.getBeratKg());
        assertTrue(saved.isReadyForDelivery());
    }
}

