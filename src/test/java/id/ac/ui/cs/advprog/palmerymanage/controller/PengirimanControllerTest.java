package id.ac.ui.cs.advprog.palmerymanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.PartialRejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.RejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.UpdateStatusRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanResponseMapper;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.PengirimanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PengirimanControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private PengirimanService pengirimanService;

	@Mock
	private HarvestResultRepository harvestResultRepository;

	@Mock
	private PengirimanResponseMapper pengirimanResponseMapper;

	@InjectMocks
	private PengirimanController pengirimanController;

	private UUID mandorId;
	private UUID supirId;
	private UUID panenId;
	private Pengiriman samplePengiriman;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(pengirimanController).build();
		objectMapper = new ObjectMapper();

		mandorId = UUID.randomUUID();
		supirId = UUID.randomUUID();
		panenId = UUID.randomUUID();

		samplePengiriman = new Pengiriman();
		samplePengiriman.setId(UUID.randomUUID());
		samplePengiriman.setSupirId(supirId.toString());
		samplePengiriman.setMandorId(mandorId.toString());
		samplePengiriman.setTotalKg(150);
		samplePengiriman.setStatus(PengirimanStatus.MEMUAT);
	}

	@Test
	void driversForMandor_blankMandor_returnsEmpty() throws Exception {
		mockMvc.perform(get("/api/mandor/drivers"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void panenSiapAngkut_filtersByMandorHeader() throws Exception {
		HarvestResult h = new HarvestResult();
		h.setId(panenId);
		h.setMandorId(mandorId);
		h.setPlantation(Plantation.builder().id(UUID.randomUUID()).build());
		h.setKgHarvested(123f);
		h.setReadyForDelivery(true);

		when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(h));

		mockMvc.perform(get("/api/mandor/panen/siap-angkut").header("X-User-Id", mandorId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].mandor_id").value(mandorId.toString()));
	}

	@Test
	void createPengiriman_success_returns200() throws Exception {
		CreatePengirimanRequest req = new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()));
		when(pengirimanService.createPengiriman(eq(mandorId.toString()), any())).thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("id", samplePengiriman.getId().toString()));

		mockMvc.perform(post("/api/mandor/pengiriman")
						.header("X-User-Id", mandorId.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(samplePengiriman.getId().toString()));
	}

	@Test
	void updateStatusSupir_success_returns200() throws Exception {
		UUID id = UUID.randomUUID();
		UpdateStatusRequest req = new UpdateStatusRequest("MENGIRIM");
		samplePengiriman.setStatus(PengirimanStatus.MENGIRIM);
		when(pengirimanService.updateStatusSupir(eq(supirId.toString()), eq(id), eq(PengirimanStatus.MENGIRIM)))
				.thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "MENGIRIM"));

		mockMvc.perform(patch("/api/supir/pengiriman/{id}/status", id)
						.header("X-User-Id", supirId.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("MENGIRIM"));
	}

	@Test
	void driversForMandor_returnsDriversFromService() throws Exception {
		when(pengirimanService.listSupirOnKebunMandor(mandorId.toString(), "supir"))
				.thenReturn(List.of(Map.of(
						"id", supirId.toString(),
						"nama", "Supir",
						"kebun_id", UUID.randomUUID().toString(),
						"kontak", "supir@example.test")));

		mockMvc.perform(get("/api/mandor/drivers")
						.header("X-User-Id", mandorId.toString())
						.param("search", "supir"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(supirId.toString()))
				.andExpect(jsonPath("$[0].nama").value("Supir"));
	}

	@Test
	void panenSiapAngkut_withoutMandorHeaderReturnsAllAndHandlesNullFields() throws Exception {
		HarvestResult h = new HarvestResult();
		h.setId(panenId);
		h.setKgHarvested(null);
		h.setReadyForDelivery(true);
		h.setStatus("APPROVED");

		when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(h));

		mockMvc.perform(get("/api/mandor/panen/siap-angkut"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].berat_kg").value(0))
				.andExpect(jsonPath("$[0].kebun_id").doesNotExist())
				.andExpect(jsonPath("$[0].mandor_id").doesNotExist());
	}

	@Test
	void pengirimanAktifMandor_returnsMappedList() throws Exception {
		when(pengirimanService.pengirimanAktifMandor(mandorId.toString())).thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "MEMUAT"));

		mockMvc.perform(get("/api/mandor/pengiriman/aktif").header("X-User-Id", mandorId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("MEMUAT"));
	}

	@Test
	void pengirimanBySupirForMandor_usesDefaultDatesWhenParamsBlank() throws Exception {
		when(pengirimanService.pengirimanBySupirForMandor(eq(mandorId.toString()), eq(supirId.toString()), any(), any()))
				.thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("supir_id", supirId.toString()));

		mockMvc.perform(get("/api/mandor/supir/{supirId}/pengiriman", supirId)
						.header("X-User-Id", mandorId.toString())
						.param("from", "")
						.param("to", ""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].supir_id").value(supirId.toString()));
	}

	@Test
	void pengirimanBySupirForMandor_parsesDateParams() throws Exception {
		when(pengirimanService.pengirimanBySupirForMandor(
				eq(mandorId.toString()), eq(supirId.toString()), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
				.thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("supir_id", supirId.toString()));

		mockMvc.perform(get("/api/mandor/supir/{supirId}/pengiriman", supirId)
						.header("X-User-Id", mandorId.toString())
						.param("from", "2026-01-01")
						.param("to", "2026-01-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].supir_id").value(supirId.toString()));
	}

	@Test
	void approveMandor_returnsMappedBody() throws Exception {
		samplePengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
		when(pengirimanService.approveByMandor(mandorId.toString(), samplePengiriman.getId()))
				.thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "PENDING_ADMIN_REVIEW"));

		mockMvc.perform(post("/api/mandor/pengiriman/{id}/approve", samplePengiriman.getId())
						.header("X-User-Id", mandorId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING_ADMIN_REVIEW"));
	}

	@Test
	void rejectMandor_returnsMappedBody() throws Exception {
		RejectRequest request = new RejectRequest("rusak");
		samplePengiriman.setStatus(PengirimanStatus.REJECTED_MANDOR);
		when(pengirimanService.rejectByMandor(mandorId.toString(), samplePengiriman.getId(), "rusak"))
				.thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "REJECTED_MANDOR"));

		mockMvc.perform(post("/api/mandor/pengiriman/{id}/reject", samplePengiriman.getId())
						.header("X-User-Id", mandorId.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED_MANDOR"));
	}

	@Test
	void pengirimanAktifSupir_returnsMappedList() throws Exception {
		when(pengirimanService.pengirimanAktifSupir(supirId.toString())).thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("id", samplePengiriman.getId().toString()));

		mockMvc.perform(get("/api/supir/pengiriman/aktif").header("X-User-Id", supirId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(samplePengiriman.getId().toString()));
	}

	@Test
	void riwayatSupir_parsesDatesAndReturnsMappedList() throws Exception {
		when(pengirimanService.riwayatSupir(
				eq(supirId.toString()), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
				.thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("supir_id", supirId.toString()));

		mockMvc.perform(get("/api/supir/pengiriman/riwayat")
						.header("X-User-Id", supirId.toString())
						.param("from", "2026-01-01")
						.param("to", "2026-01-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].supir_id").value(supirId.toString()));
	}

	@Test
	void pendingAdmin_parsesOptionalDate() throws Exception {
		when(pengirimanService.pendingAdmin("mandor", LocalDate.of(2026, 5, 21))).thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "PENDING_ADMIN_REVIEW"));

		mockMvc.perform(get("/api/admin/pengiriman/pending")
						.param("mandor", "mandor")
						.param("date", "2026-05-21"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("PENDING_ADMIN_REVIEW"));
	}

	@Test
	void pendingAdmin_allowsBlankDate() throws Exception {
		when(pengirimanService.pendingAdmin(null, null)).thenReturn(List.of(samplePengiriman));
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "PENDING_ADMIN_REVIEW"));

		mockMvc.perform(get("/api/admin/pengiriman/pending").param("date", ""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].status").value("PENDING_ADMIN_REVIEW"));
	}

	@Test
	void detailAdmin_returnsMappedBody() throws Exception {
		when(pengirimanService.getById(samplePengiriman.getId())).thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("id", samplePengiriman.getId().toString()));

		mockMvc.perform(get("/api/admin/pengiriman/{id}", samplePengiriman.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(samplePengiriman.getId().toString()));
	}

	@Test
	void approveAdmin_returnsMappedBody() throws Exception {
		samplePengiriman.setStatus(PengirimanStatus.APPROVED_ADMIN);
		when(pengirimanService.approveByAdmin(samplePengiriman.getId())).thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "APPROVED_ADMIN"));

		mockMvc.perform(post("/api/admin/pengiriman/{id}/approve", samplePengiriman.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED_ADMIN"));
	}

	@Test
	void partialRejectAdmin_returnsMappedBody() throws Exception {
		PartialRejectRequest request = new PartialRejectRequest(100, "kurang");
		samplePengiriman.setStatus(PengirimanStatus.PARTIAL_REJECTED_ADMIN);
		when(pengirimanService.partialRejectByAdmin(samplePengiriman.getId(), 100, "kurang"))
				.thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "PARTIAL_REJECTED_ADMIN"));

		mockMvc.perform(post("/api/admin/pengiriman/{id}/partial-reject", samplePengiriman.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PARTIAL_REJECTED_ADMIN"));
	}

	@Test
	void rejectAdmin_returnsMappedBody() throws Exception {
		RejectRequest request = new RejectRequest("ditolak");
		samplePengiriman.setStatus(PengirimanStatus.REJECTED_ADMIN);
		when(pengirimanService.rejectByAdmin(samplePengiriman.getId(), "ditolak")).thenReturn(samplePengiriman);
		when(pengirimanResponseMapper.toResponse(samplePengiriman)).thenReturn(Map.of("status", "REJECTED_ADMIN"));

		mockMvc.perform(post("/api/admin/pengiriman/{id}/reject", samplePengiriman.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED_ADMIN"));
	}

	@Test
	void controllerMethodsPreferAuthenticationName() {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(mandorId.toString());
		when(pengirimanService.listSupirOnKebunMandor(mandorId.toString(), ""))
				.thenReturn(List.of(Map.of("id", supirId.toString())));

		var response = pengirimanController.driversForMandor("ignored", authentication, "");

		assertEquals(1, response.getBody().size());
		verify(pengirimanService).listSupirOnKebunMandor(mandorId.toString(), "");
	}

}
