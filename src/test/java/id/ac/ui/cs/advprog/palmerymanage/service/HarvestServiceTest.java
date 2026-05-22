package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.PlantationResponseDto;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceTest {

    @Mock
    private HarvestResultRepository harvestResultRepository;
    
    @Mock
    private PlantationService plantationService;

    @Mock
    private PlantationValidationService plantationValidationService;

    @Mock
    private HarvestEventPublisher eventPublisher;

    @Mock
    private WorkerAssignmentService workerAssignmentService;

    @Mock
    private id.ac.ui.cs.advprog.palmerymanage.service.validation.HarvestValidator validator;

    private HarvestService harvestService;

    private UUID workerId;
    private UUID mandorId;
    private UUID plantationId;
    private UUID harvestId;
    private HarvestRequestDto validRequest;
    private HarvestResult pendingHarvest;

    @BeforeEach
    void setUp() {
        harvestService = new HarvestService(harvestResultRepository, plantationService, plantationValidationService, eventPublisher, workerAssignmentService, List.of(validator));
        org.springframework.test.util.ReflectionTestUtils.setField(harvestService, "useDummyAssignment", true);
        workerId = UUID.randomUUID();
        mandorId = UUID.randomUUID();
        plantationId = UUID.randomUUID();
        harvestId = UUID.randomUUID();

        validRequest = new HarvestRequestDto();
        validRequest.setPlantationId(plantationId);
        validRequest.setMandorId(mandorId);
        validRequest.setHarvestDate(LocalDate.now());
        validRequest.setKgHarvested(100f);
        validRequest.setNotes("Panen hari ini lancar");

        Plantation plantation = Plantation.builder().id(plantationId).name("Kebun A").build();

        pendingHarvest = HarvestResult.builder()
                .id(harvestId)
                .workerId(workerId)
                .mandorId(mandorId)
                .plantation(plantation)
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("Panen hari ini lancar")
                .readyForDelivery(false)
                .status("PENDING")
                .build();
    }


    @Test
    void submitHarvest_success() {

        when(harvestResultRepository.save(any())).thenReturn(pendingHarvest);

        HarvestResult result = harvestService.submitHarvest(workerId, validRequest);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(harvestResultRepository).save(any());
    }

    @Test
    void submitHarvest_withPhotos_success() {
        HarvestRequestDto.PhotoDto photo = new HarvestRequestDto.PhotoDto();
        photo.setUrl("http://rustfs.palmery.com/foto.jpg");
        photo.setFilename("foto.jpg");
        photo.setSizeBytes(10000);
        validRequest.setPhotos(List.of(photo));


        when(harvestResultRepository.save(any())).thenReturn(pendingHarvest);

        HarvestResult result = harvestService.submitHarvest(workerId, validRequest);

        assertNotNull(result);
        verify(harvestResultRepository).save(any());
    }



    @Test
    void validateHarvest_approve_success() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("APPROVED");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));
        when(harvestResultRepository.save(any())).thenReturn(pendingHarvest);

        HarvestResult result = harvestService.validateHarvest(mandorId, harvestId, request);

        assertEquals("APPROVED", result.getStatus());
        assertTrue(result.getReadyForDelivery());
        assertNotNull(result.getValidatedAt());
        verify(eventPublisher).publishHarvestApproved(any(HarvestApprovedEvent.class));
    }

    @Test
    void validateHarvest_approve_eventContainsCorrectData() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("APPROVED");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));
        when(harvestResultRepository.save(any())).thenReturn(pendingHarvest);

        harvestService.validateHarvest(mandorId, harvestId, request);

        ArgumentCaptor<HarvestApprovedEvent> captor = ArgumentCaptor.forClass(HarvestApprovedEvent.class);
        verify(eventPublisher).publishHarvestApproved(captor.capture());

        HarvestApprovedEvent event = captor.getValue();
        assertEquals(harvestId, event.harvestId());
        assertEquals(workerId.toString(), event.workerId());
        assertEquals(mandorId.toString(), event.mandorId());
        assertEquals(100f, event.kgHarvested());
    }

    @Test
    void validateHarvest_reject_success() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("REJECTED");
        request.setRejectionReason("Foto tidak jelas");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));
        when(harvestResultRepository.save(any())).thenReturn(pendingHarvest);

        HarvestResult result = harvestService.validateHarvest(mandorId, harvestId, request);

        assertEquals("REJECTED", result.getStatus());
        verify(eventPublisher, never()).publishHarvestApproved(any());
    }

    @Test
    void validateHarvest_rejectWithoutReason_throwsException() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("REJECTED");
        request.setRejectionReason("");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Alasan penolakan wajib diisi jika menolak laporan.", ex.getMessage());
    }

    @Test
    void validateHarvest_rejectWithNullReason_throwsException() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("REJECTED");
        request.setRejectionReason(null);

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Alasan penolakan wajib diisi jika menolak laporan.", ex.getMessage());
    }

    @Test
    void validateHarvest_alreadyApproved_throwsException() {
        pendingHarvest.setStatus("APPROVED");
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("REJECTED");
        request.setRejectionReason("alasan");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Laporan ini sudah divalidasi dan tidak dapat diubah lagi.", ex.getMessage());
    }

    @Test
    void validateHarvest_alreadyRejected_throwsException() {
        pendingHarvest.setStatus("REJECTED");
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("APPROVED");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Laporan ini sudah divalidasi dan tidak dapat diubah lagi.", ex.getMessage());
    }

    @Test
    void validateHarvest_nullStatus_throwsException() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Status validasi tidak boleh kosong", ex.getMessage());
    }

    @Test
    void validateHarvest_emptyStatus_throwsException() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("  ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Status validasi tidak boleh kosong", ex.getMessage());
    }

    @Test
    void validateHarvest_harvestNotFound_throwsException() {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("APPROVED");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Laporan Harvest tidak ditemukan", ex.getMessage());
    }


    @Test
    void getHarvestById_found() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));

        HarvestResult result = harvestService.getHarvestById(harvestId);

        assertNotNull(result);
        assertEquals(harvestId, result.getId());
    }

    @Test
    void getHarvestById_notFound_throwsException() {
        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.getHarvestById(harvestId));
        assertEquals("Laporan Harvest tidak ditemukan", ex.getMessage());
    }


    @Test
    void getHarvestsByWorkerId_success() {
        when(harvestResultRepository.findByWorkerId(workerId)).thenReturn(List.of(pendingHarvest));

        List<HarvestResult> results = harvestService.getHarvestsByWorkerId(workerId);

        assertEquals(1, results.size());
    }

    @Test
    void getHarvestsByWorkerId_nullWorkerId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.getHarvestsByWorkerId(null));
        assertEquals("Worker ID tidak boleh kosong", ex.getMessage());
    }


    @Test
    void getAllHarvests_success() {
        when(harvestResultRepository.findAll()).thenReturn(List.of(pendingHarvest));

        List<HarvestResult> results = harvestService.getAllHarvests();

        assertEquals(1, results.size());
    }


    @Test
    void getBuruhHistory_success() {
        when(harvestResultRepository.findBuruhHistory(workerId, null, null, null))
                .thenReturn(List.of(pendingHarvest));

        List<HarvestResult> results = harvestService.getBuruhHistory(workerId, null, null, null);

        assertEquals(1, results.size());
    }

    @Test
    void getBuruhHistory_withFilters_success() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();

        when(harvestResultRepository.findBuruhHistory(workerId, start, end, "PENDING"))
                .thenReturn(List.of(pendingHarvest));

        List<HarvestResult> results = harvestService.getBuruhHistory(workerId, start, end, "PENDING");

        assertEquals(1, results.size());
    }

    @Test
    void getBuruhHistory_nullWorkerId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> harvestService.getBuruhHistory(null, null, null, null));
        assertEquals("Worker ID tidak boleh kosong", ex.getMessage());
    }



    @Test
    void getMandorHistory_success() {
        when(harvestResultRepository.findMandorHistory(null, null))
                .thenReturn(List.of(pendingHarvest));

        List<HarvestResult> results = harvestService.getMandorHistory(null, null, null);

        assertEquals(1, results.size());
    }

    @Test
    void getMandorHistory_withFilters_success() {
        LocalDate date = LocalDate.now();

        when(harvestResultRepository.findMandorHistory(date, workerId))
                .thenReturn(List.of(pendingHarvest));

        List<HarvestResult> results = harvestService.getMandorHistory(null, date, workerId);

        assertEquals(1, results.size());
    }

    @Test
    void validateHarvest_notAnakBuah_apiThrowsException() {
        org.springframework.test.util.ReflectionTestUtils.setField(harvestService, "useDummyAssignment", false);
        org.springframework.test.util.ReflectionTestUtils.setField(harvestService, "assignmentApiUrl", "http://localhost:8080/api/assignment");
        
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("APPROVED");

        when(harvestResultRepository.findById(harvestId)).thenReturn(Optional.of(pendingHarvest));

        // Trigger RestClientException to simulate API failure and trigger IllegalStateException

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> harvestService.validateHarvest(mandorId, harvestId, request));
        assertEquals("Akses Ditolak: Buruh ini bukan di bawah pengawasan Anda!", ex.getMessage());
    }
}