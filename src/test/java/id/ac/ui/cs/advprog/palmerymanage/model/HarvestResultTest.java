package id.ac.ui.cs.advprog.palmerymanage.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HarvestResultTest {

    private HarvestResult buildSample() {
        return HarvestResult.builder()
                .id(UUID.randomUUID())
                .workerId(UUID.randomUUID())
                .mandorId(UUID.randomUUID())
                .plantation(Plantation.builder().id(UUID.randomUUID()).name("Kebun A").build())
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("Panen hari ini lancar")
                .readyForDelivery(false)
                .status("PENDING")
                .build();
    }

    @Test
    void harvestResult_builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        UUID workerId = UUID.randomUUID();
        UUID mandorId = UUID.randomUUID();
        Plantation plantation = Plantation.builder().id(UUID.randomUUID()).name("Kebun A").build();
        LocalDate date = LocalDate.now();

        HarvestResult harvest = HarvestResult.builder()
                .id(id)
                .workerId(workerId)
                .mandorId(mandorId)
                .plantation(plantation)
                .harvestDate(date)
                .kgHarvested(150f)
                .notes("catatan panen")
                .readyForDelivery(false)
                .status("PENDING")
                .build();

        assertEquals(id, harvest.getId());
        assertEquals(workerId, harvest.getWorkerId());
        assertEquals(mandorId, harvest.getMandorId());
        assertEquals(plantation, harvest.getPlantation());
        assertEquals(date, harvest.getHarvestDate());
        assertEquals(150f, harvest.getKgHarvested());
        assertEquals("catatan panen", harvest.getNotes());
        assertFalse(harvest.getReadyForDelivery());
        assertEquals("PENDING", harvest.getStatus());
    }

    @Test
    void harvestResult_defaultPhotosIsEmptyList() {
        HarvestResult harvest = buildSample();
        assertNotNull(harvest.getPhotos());
        assertTrue(harvest.getPhotos().isEmpty());
    }

    @Test
    void harvestResult_setStatus_updatesStatus() {
        HarvestResult harvest = buildSample();
        harvest.setStatus("APPROVED");
        assertEquals("APPROVED", harvest.getStatus());
    }

    @Test
    void harvestResult_setReadyForDelivery_updatesValue() {
        HarvestResult harvest = buildSample();
        harvest.setReadyForDelivery(true);
        assertTrue(harvest.getReadyForDelivery());
    }

    @Test
    void harvestResult_setRejectionReason_updatesValue() {
        HarvestResult harvest = buildSample();
        harvest.setRejectionReason("Foto tidak jelas");
        assertEquals("Foto tidak jelas", harvest.getRejectionReason());
    }

    @Test
    void harvestResult_setValidatedAt_updatesValue() {
        HarvestResult harvest = buildSample();
        LocalDateTime now = LocalDateTime.now();
        harvest.setValidatedAt(now);
        assertEquals(now, harvest.getValidatedAt());
    }

    @Test
    void harvestResult_noArgsConstructor_works() {
        HarvestResult harvest = new HarvestResult();
        assertNotNull(harvest);
    }

    @Test
    void harvestResult_allArgsConstructor_works() {
        UUID id = UUID.randomUUID();
        Plantation plantation = Plantation.builder().id(UUID.randomUUID()).build();
        HarvestResult harvest = new HarvestResult(
                id, UUID.randomUUID(), UUID.randomUUID(), plantation,
            LocalDate.now(), 100f, 100f, "Notes", false, "PENDING", null,
            null, LocalDateTime.now(), new ArrayList<>()
        );
        assertEquals(id, harvest.getId());
    }

    @Test
    void harvestResult_setPhotos_updatesPhotos() {
        HarvestResult harvest = buildSample();
        HarvestPhoto photo = HarvestPhoto.builder()
                .url("http://rustfs.com/foto.jpg")
                .filename("foto.jpg")
                .sizeBytes(10000)
                .harvestResult(harvest)
                .build();

        harvest.setPhotos(new ArrayList<>());
        harvest.getPhotos().add(photo);

        assertEquals(1, harvest.getPhotos().size());
    }
}