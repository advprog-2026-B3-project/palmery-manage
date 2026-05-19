package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.PlantationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlantationCoordinateValidatorTest {

    private PlantationCoordinateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PlantationCoordinateValidator();
    }

    @Test
    void shouldDetectOverlappingPolygons() {
        // Polygon A: a square from (0,0) to (10,10)
        PlantationRequestDto incoming = PlantationRequestDto.builder()
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(10.0)
                .coordBrLat(10.0).coordBrLon(10.0)
                .coordBlLat(10.0).coordBlLon(0.0)
                .build();

        // Polygon B: overlaps with A (5,5) to (15,15)
        Plantation existing = Plantation.builder()
                .coordTlLat(5.0).coordTlLon(5.0)
                .coordTrLat(5.0).coordTrLon(15.0)
                .coordBrLat(15.0).coordBrLon(15.0)
                .coordBlLat(15.0).coordBlLon(5.0)
                .build();

        boolean result = validator.polygonsOverlap(incoming, existing);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotDetectOverlapForSeparatePolygons() {
        // Polygon A: (0,0) to (5,5)
        PlantationRequestDto incoming = PlantationRequestDto.builder()
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(5.0)
                .coordBrLat(5.0).coordBrLon(5.0)
                .coordBlLat(5.0).coordBlLon(0.0)
                .build();

        // Polygon B: (10,10) to (15,15) - completely separate
        Plantation existing = Plantation.builder()
                .coordTlLat(10.0).coordTlLon(10.0)
                .coordTrLat(10.0).coordTrLon(15.0)
                .coordBrLat(15.0).coordBrLon(15.0)
                .coordBlLat(15.0).coordBlLon(10.0)
                .build();

        boolean result = validator.polygonsOverlap(incoming, existing);

        assertThat(result).isFalse();
    }

    @Test
    void shouldDetectContainedPolygon() {
        // Polygon A: large (0,0) to (20,20)
        PlantationRequestDto incoming = PlantationRequestDto.builder()
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(20.0)
                .coordBrLat(20.0).coordBrLon(20.0)
                .coordBlLat(20.0).coordBlLon(0.0)
                .build();

        // Polygon B: small inside A (5,5) to (10,10)
        Plantation existing = Plantation.builder()
                .coordTlLat(5.0).coordTlLon(5.0)
                .coordTrLat(5.0).coordTrLon(10.0)
                .coordBrLat(10.0).coordBrLon(10.0)
                .coordBlLat(10.0).coordBlLon(5.0)
                .build();

        boolean result = validator.polygonsOverlap(incoming, existing);

        assertThat(result).isTrue();
    }

    @Test
    void shouldCheckOverlapWithMultiplePlantations() {
        PlantationRequestDto incoming = PlantationRequestDto.builder()
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(10.0)
                .coordBrLat(10.0).coordBrLon(10.0)
                .coordBlLat(10.0).coordBlLon(0.0)
                .build();

        Plantation noOverlap = Plantation.builder()
                .coordTlLat(20.0).coordTlLon(20.0)
                .coordTrLat(20.0).coordTrLon(30.0)
                .coordBrLat(30.0).coordBrLon(30.0)
                .coordBlLat(30.0).coordBlLon(20.0)
                .build();

        Plantation hasOverlap = Plantation.builder()
                .coordTlLat(5.0).coordTlLon(5.0)
                .coordTrLat(5.0).coordTrLon(15.0)
                .coordBrLat(15.0).coordBrLon(15.0)
                .coordBlLat(15.0).coordBlLon(5.0)
                .build();

        boolean result = validator.hasOverlapWithAny(incoming, List.of(noOverlap, hasOverlap));

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoOverlapWithAny() {
        PlantationRequestDto incoming = PlantationRequestDto.builder()
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(5.0)
                .coordBrLat(5.0).coordBrLon(5.0)
                .coordBlLat(5.0).coordBlLon(0.0)
                .build();

        Plantation far1 = Plantation.builder()
                .coordTlLat(20.0).coordTlLon(20.0)
                .coordTrLat(20.0).coordTrLon(30.0)
                .coordBrLat(30.0).coordBrLon(30.0)
                .coordBlLat(30.0).coordBlLon(20.0)
                .build();

        Plantation far2 = Plantation.builder()
                .coordTlLat(40.0).coordTlLon(40.0)
                .coordTrLat(40.0).coordTrLon(50.0)
                .coordBrLat(50.0).coordBrLon(50.0)
                .coordBlLat(50.0).coordBlLon(40.0)
                .build();

        boolean result = validator.hasOverlapWithAny(incoming, List.of(far1, far2));

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoExistingPlantations() {
        PlantationRequestDto incoming = PlantationRequestDto.builder()
                .coordTlLat(0.0).coordTlLon(0.0)
                .coordTrLat(0.0).coordTrLon(5.0)
                .coordBrLat(5.0).coordBrLon(5.0)
                .coordBlLat(5.0).coordBlLon(0.0)
                .build();

        boolean result = validator.hasOverlapWithAny(incoming, List.of());

        assertThat(result).isFalse();
    }
}
