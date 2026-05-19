package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment.PersonnelRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantationAssignmentRepository extends JpaRepository<PlantationAssignment, UUID> {

    List<PlantationAssignment> findByPlantationId(UUID plantationId);

    List<PlantationAssignment> findByPlantationIdAndRole(UUID plantationId, PersonnelRole role);

    Optional<PlantationAssignment> findByPlantationIdAndPersonnelIdAndRole(
            UUID plantationId, UUID personnelId, PersonnelRole role);

    boolean existsByPlantationIdAndRole(UUID plantationId, PersonnelRole role);

    boolean existsByPlantationIdAndPersonnelIdAndRole(UUID plantationId, UUID personnelId, PersonnelRole role);

    List<PlantationAssignment> findByPersonnelIdAndRole(UUID personnelId, PersonnelRole role);
}
