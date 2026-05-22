package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.MandorWorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MandorWorkerAssignmentRepository extends JpaRepository<MandorWorkerAssignment, UUID> {

    Optional<MandorWorkerAssignment> findByWorkerId(UUID workerId);

    List<MandorWorkerAssignment> findByMandorId(UUID mandorId);

    boolean existsByWorkerIdAndMandorId(UUID workerId, UUID mandorId);

    long countByMandorId(UUID mandorId);
}
