package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.PlantationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.PlantationResponseDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.PlantationSummaryDto;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlantationMapperTest {

    private PlantationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlantationMapper();
    }

    @Test
    void shouldMapEntityToResponseDto() {
        Plantation plantation = Plantation.builder()
                .id(UUID.randomUUID())
                .name("Kebun A")
                .code("KA-001")
                .areaHa(100.0)
                .coordTlLat(-6.1).coordTlLon(106.8)
                .coordTrLat(-6.1).coordTrLon(106.9)
                .coordBrLat(-6.2).coordBrLon(106.9)
                .coordBlLat(-6.2).coordBlLon(106.8)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PlantationResponseDto dto = mapper.toResponseDto(plantation);

        assertThat(dto.getId()).isEqualTo(plantation.getId());
        assertThat(dto.getName()).isEqualTo("Kebun A");
        assertThat(dto.getCode()).isEqualTo("KA-001");
        assertThat(dto.getAreaHa()).isEqualTo(100.0);
        assertThat(dto.getCoordTlLat()).isEqualTo(-6.1);
        assertThat(dto.getIsActive()).isTrue();
    }

    @Test
    void shouldMapEntityToSummaryDto() {
        Plantation plantation = Plantation.builder()
                .id(UUID.randomUUID())
                .name("Kebun B")
                .code("KB-002")
                .areaHa(75.0)
                .isActive(true)
                .build();

        PlantationSummaryDto dto = mapper.toSummaryDto(plantation);

        assertThat(dto.getId()).isEqualTo(plantation.getId());
        assertThat(dto.getName()).isEqualTo("Kebun B");
        assertThat(dto.getCode()).isEqualTo("KB-002");
        assertThat(dto.getAreaHa()).isEqualTo(75.0);
        assertThat(dto.getIsActive()).isTrue();
    }

    @Test
    void shouldMapRequestDtoToEntity() {
        PlantationRequestDto request = PlantationRequestDto.builder()
                .name("Kebun C")
                .code("KC-003")
                .areaHa(200.0)
                .coordTlLat(-6.5).coordTlLon(107.0)
                .coordTrLat(-6.5).coordTrLon(107.1)
                .coordBrLat(-6.6).coordBrLon(107.1)
                .coordBlLat(-6.6).coordBlLon(107.0)
                .build();

        Plantation entity = mapper.toEntity(request);

        assertThat(entity.getName()).isEqualTo("Kebun C");
        assertThat(entity.getCode()).isEqualTo("KC-003");
        assertThat(entity.getAreaHa()).isEqualTo(200.0);
        assertThat(entity.getCoordTlLat()).isEqualTo(-6.5);
        assertThat(entity.getCoordBlLon()).isEqualTo(107.0);
    }

    @Test
    void shouldUpdateEntityFromDto() {
        Plantation existing = Plantation.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .code("OLD-001")
                .areaHa(50.0)
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(0.0)
                .coordBrLat(0.0).coordBrLon(0.0)
                .coordBlLat(0.0).coordBlLon(0.0)
                .build();

        PlantationRequestDto update = PlantationRequestDto.builder()
                .name("New Name")
                .code("NEW-002")
                .areaHa(150.0)
                .coordTlLat(-7.0).coordTlLon(110.0)
                .coordTrLat(-7.0).coordTrLon(110.1)
                .coordBrLat(-7.1).coordBrLon(110.1)
                .coordBlLat(-7.1).coordBlLon(110.0)
                .build();

        mapper.updateEntityFromDto(existing, update);

        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getCode()).isEqualTo("NEW-002");
        assertThat(existing.getAreaHa()).isEqualTo(150.0);
        assertThat(existing.getCoordTlLat()).isEqualTo(-7.0);
    }
}
