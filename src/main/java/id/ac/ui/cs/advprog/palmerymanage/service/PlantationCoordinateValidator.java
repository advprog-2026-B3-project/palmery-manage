package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.PlantationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.springframework.stereotype.Component;

import java.awt.geom.Path2D;
import java.util.List;

/**
 * Responsible solely for coordinate-related validations (SRP).
 * Extracted from service to keep PlantationServiceImpl focused on orchestration.
 */
@Component
public class PlantationCoordinateValidator {

    /**
     * Checks whether two quadrilateral polygons (each defined by 4 lat/lon points) overlap.
     * Uses Java's Path2D for polygon intersection check.
     */
    public boolean polygonsOverlap(PlantationRequestDto incoming, Plantation existing) {
        Path2D incomingPolygon = buildPolygon(incoming);
        Path2D existingPolygon = buildPolygonFromEntity(existing);

        double[][] incomingPoints = extractPoints(incoming);
        double[][] existingPoints = extractPointsFromEntity(existing);

        // Check if any vertex of incoming is inside existing polygon
        for (double[] point : incomingPoints) {
            if (existingPolygon.contains(point[0], point[1])) {
                return true;
            }
        }

        // Check if any vertex of existing is inside incoming polygon
        for (double[] point : existingPoints) {
            if (incomingPolygon.contains(point[0], point[1])) {
                return true;
            }
        }

        return false;
    }

    public boolean hasOverlapWithAny(PlantationRequestDto incoming, List<Plantation> others) {
        return others.stream().anyMatch(other -> polygonsOverlap(incoming, other));
    }

    private Path2D buildPolygon(PlantationRequestDto dto) {
        Path2D path = new Path2D.Double();
        path.moveTo(dto.getCoordTlLat(), dto.getCoordTlLon());
        path.lineTo(dto.getCoordTrLat(), dto.getCoordTrLon());
        path.lineTo(dto.getCoordBrLat(), dto.getCoordBrLon());
        path.lineTo(dto.getCoordBlLat(), dto.getCoordBlLon());
        path.closePath();
        return path;
    }

    private Path2D buildPolygonFromEntity(Plantation p) {
        Path2D path = new Path2D.Double();
        path.moveTo(p.getCoordTlLat(), p.getCoordTlLon());
        path.lineTo(p.getCoordTrLat(), p.getCoordTrLon());
        path.lineTo(p.getCoordBrLat(), p.getCoordBrLon());
        path.lineTo(p.getCoordBlLat(), p.getCoordBlLon());
        path.closePath();
        return path;
    }

    private double[][] extractPoints(PlantationRequestDto dto) {
        return new double[][]{
            {dto.getCoordTlLat(), dto.getCoordTlLon()},
            {dto.getCoordTrLat(), dto.getCoordTrLon()},
            {dto.getCoordBrLat(), dto.getCoordBrLon()},
            {dto.getCoordBlLat(), dto.getCoordBlLon()}
        };
    }

    private double[][] extractPointsFromEntity(Plantation p) {
        return new double[][]{
            {p.getCoordTlLat(), p.getCoordTlLon()},
            {p.getCoordTrLat(), p.getCoordTrLon()},
            {p.getCoordBrLat(), p.getCoordBrLon()},
            {p.getCoordBlLat(), p.getCoordBlLon()}
        };
    }
}
