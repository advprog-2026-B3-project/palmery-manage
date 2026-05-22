package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class HarvestResultRepositoryTest {

    @Autowired
    private HarvestResultRepository harvestResultRepository;

    @Autowired
    private PlantationRepository plantationRepository;

    private UUID workerId;
    private UUID mandorId;
    private Plantation savedPlantation;

    @BeforeEach
    void setUp() {
        harvestResultRepository.deleteAll();
        plantationRepository.deleteAll();
        workerId = UUID.randomUUID();
        mandorId = UUID.randomUUID();
        
        savedPlantation = plantationRepository.save(Plantation.builder()
                .name("Kebun A")
                .code("KBN-" + UUID.randomUUID().toString().substring(0, 8))
                .areaHa(10.0)
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(1.0)
                .coordBrLat(1.0).coordBrLon(1.0)
                .coordBlLat(1.0).coordBlLon(0.0)
                .isActive(true)
                .build());
    }

    private HarvestResult buildHarvest(UUID wId, LocalDate date, String status) {
        return HarvestResult.builder()
                .workerId(wId)
                .mandorId(mandorId)
                .plantation(savedPlantation)
                .harvestDate(date)
                .kgHarvested(100f)
                .notes("Test panen")
                .readyForDelivery(false)
                .status(status)
                .build();
    }

    // existsByWorkerIdAndHarvestDate

    @Test
    void existsByWorkerIdAndHarvestDate_exists_returnsTrue() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));

        boolean exists = harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, LocalDate.now());

        assertTrue(exists);
    }

    @Test
    void existsByWorkerIdAndHarvestDate_notExists_returnsFalse() {
        boolean exists = harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, LocalDate.now());

        assertFalse(exists);
    }

    @Test
    void existsByWorkerIdAndHarvestDate_differentDate_returnsFalse() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(1), "PENDING"));

        boolean exists = harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, LocalDate.now());

        assertFalse(exists);
    }

    @Test
    void existsByWorkerIdAndHarvestDate_differentWorker_returnsFalse() {
        harvestResultRepository.save(buildHarvest(UUID.randomUUID(), LocalDate.now(), "PENDING"));

        boolean exists = harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, LocalDate.now());

        assertFalse(exists);
    }

    // findByWorkerId

    @Test
    void findByWorkerId_returnsAllHarvestsForWorker() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(1), "APPROVED"));
        harvestResultRepository.save(buildHarvest(UUID.randomUUID(), LocalDate.now(), "PENDING"));

        List<HarvestResult> results = harvestResultRepository.findByWorkerId(workerId);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(h -> h.getWorkerId().equals(workerId)));
    }

    @Test
    void findByWorkerId_noHarvests_returnsEmptyList() {
        List<HarvestResult> results = harvestResultRepository.findByWorkerId(workerId);

        assertTrue(results.isEmpty());
    }

    // findBuruhHistory — filter startDate

    @Test
    void findBuruhHistory_noFilter_returnsAll() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(1), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findBuruhHistory(workerId, null, null, null);

        assertEquals(2, results.size());
    }

    @Test
    void findBuruhHistory_filterByStartDate_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(10), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findBuruhHistory(
                workerId, LocalDate.now().minusDays(5), null, null);

        assertEquals(1, results.size());
        assertEquals("PENDING", results.get(0).getStatus());
    }

    @Test
    void findBuruhHistory_filterByEndDate_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(10), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findBuruhHistory(
                workerId, null, LocalDate.now().minusDays(5), null);

        assertEquals(1, results.size());
        assertEquals("APPROVED", results.get(0).getStatus());
    }

    @Test
    void findBuruhHistory_filterByStatus_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(1), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findBuruhHistory(
                workerId, null, null, "APPROVED");

        assertEquals(1, results.size());
        assertEquals("APPROVED", results.get(0).getStatus());
    }

    @Test
    void findBuruhHistory_filterByAllParams_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "APPROVED"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(10), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findBuruhHistory(
                workerId,
                LocalDate.now().minusDays(5),
                LocalDate.now(),
                "APPROVED");

        assertEquals(1, results.size());
    }

    @Test
    void findBuruhHistory_differentWorker_returnsEmpty() {
        harvestResultRepository.save(buildHarvest(UUID.randomUUID(), LocalDate.now(), "PENDING"));

        List<HarvestResult> results = harvestResultRepository.findBuruhHistory(workerId, null, null, null);

        assertTrue(results.isEmpty());
    }

    // findMandorHistory

    @Test
    void findMandorHistory_noFilter_returnsAll() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(UUID.randomUUID(), LocalDate.now().minusDays(1), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findMandorHistory(null, null);

        assertEquals(2, results.size());
    }

    @Test
    void findMandorHistory_filterByDate_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(UUID.randomUUID(), LocalDate.now().minusDays(1), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findMandorHistory(LocalDate.now(), null);

        assertEquals(1, results.size());
        assertEquals(workerId, results.get(0).getWorkerId());
    }

    @Test
    void findMandorHistory_filterByWorkerId_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(UUID.randomUUID(), LocalDate.now(), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findMandorHistory(null, workerId);

        assertEquals(1, results.size());
        assertEquals(workerId, results.get(0).getWorkerId());
    }

    @Test
    void findMandorHistory_filterByDateAndWorkerId_returnsFiltered() {
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now(), "PENDING"));
        harvestResultRepository.save(buildHarvest(workerId, LocalDate.now().minusDays(1), "APPROVED"));

        List<HarvestResult> results = harvestResultRepository.findMandorHistory(LocalDate.now(), workerId);

        assertEquals(1, results.size());
        assertEquals("PENDING", results.get(0).getStatus());
    }
}