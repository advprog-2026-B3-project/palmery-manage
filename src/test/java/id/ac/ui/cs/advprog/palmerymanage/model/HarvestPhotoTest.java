package id.ac.ui.cs.advprog.palmerymanage.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HarvestPhotoTest {

    private HarvestResult buildHarvestResult() {
        Plantation plantation = Plantation.builder().id(UUID.randomUUID()).build();
        return HarvestResult.builder()
                .id(UUID.randomUUID())
                .workerId(UUID.randomUUID())
                .mandorId(UUID.randomUUID())
                .plantation(plantation)
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("notes")
                .readyForDelivery(false)
                .status("PENDING")
                .build();
    }

    @Test
    void harvestPhoto_builderSetsAllFields() {
        HarvestResult harvest = buildHarvestResult();
        UUID id = UUID.randomUUID();

        HarvestPhoto photo = HarvestPhoto.builder()
                .id(id)
                .harvestResult(harvest)
                .url("http://rustfs.com/foto.jpg")
                .filename("foto.jpg")
                .sizeBytes(10000)
                .build();

        assertEquals(id, photo.getId());
        assertEquals(harvest, photo.getHarvestResult());
        assertEquals("http://rustfs.com/foto.jpg", photo.getUrl());
        assertEquals("foto.jpg", photo.getFilename());
        assertEquals(10000, photo.getSizeBytes());
    }

    @Test
    void harvestPhoto_setUrl_updatesValue() {
        HarvestPhoto photo = new HarvestPhoto();
        photo.setUrl("http://new-url.com/foto.jpg");
        assertEquals("http://new-url.com/foto.jpg", photo.getUrl());
    }

    @Test
    void harvestPhoto_setFilename_updatesValue() {
        HarvestPhoto photo = new HarvestPhoto();
        photo.setFilename("newfile.jpg");
        assertEquals("newfile.jpg", photo.getFilename());
    }

    @Test
    void harvestPhoto_setSizeBytes_updatesValue() {
        HarvestPhoto photo = new HarvestPhoto();
        photo.setSizeBytes(20000);
        assertEquals(20000, photo.getSizeBytes());
    }

    @Test
    void harvestPhoto_noArgsConstructor_works() {
        HarvestPhoto photo = new HarvestPhoto();
        assertNotNull(photo);
    }
}