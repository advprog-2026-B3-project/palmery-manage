package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    List<Delivery> findBySupirIdAndStatusIn(String supirId, List<DeliveryStatus> statuses);
    List<Delivery> findBySupirIdAndCreatedAtBetween(String supirId, Instant from, Instant to);
    List<Delivery> findByKebunIdAndStatusIn(String kebunId, List<DeliveryStatus> statuses);
    List<Delivery> findByStatus(DeliveryStatus status);
}

