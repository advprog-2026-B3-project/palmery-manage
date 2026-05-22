package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.WorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, UUID> {

    List<WorkerAssignment> findByMandorId(UUID mandorId);

    boolean existsByWorkerIdAndMandorId(UUID workerId, UUID mandorId);
}
