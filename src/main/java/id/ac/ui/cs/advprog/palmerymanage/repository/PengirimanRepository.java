package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.AdminApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.ApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PengirimanRepository extends JpaRepository<Pengiriman, UUID> {

    List<Pengiriman> findBySupirIdAndStatusIn(String supirId, List<PengirimanStatus> statuses);

    List<Pengiriman> findBySupirIdAndCreatedAtBetween(String supirId, Instant from, Instant to);

    List<Pengiriman> findByMandorIdAndStatusIn(String mandorId, List<PengirimanStatus> statuses);

    List<Pengiriman> findBySupirIdAndMandorIdAndCreatedAtBetween(
            String supirId, String mandorId, Instant from, Instant to);

    // New: query by approval statuses
    List<Pengiriman> findByMandorApprovalStatusAndAdminApprovalStatus(
            ApprovalStatus mandorApprovalStatus, AdminApprovalStatus adminApprovalStatus);

    // Legacy queries (kept for backward compat)
    List<Pengiriman> findByStatus(PengirimanStatus status);

    List<Pengiriman> findByStatusAndCreatedAtBetween(
            PengirimanStatus status, Instant from, Instant to);

    List<Pengiriman> findByStatusAndMandorIdContainingIgnoreCase(
            PengirimanStatus status, String mandorSearch);

    List<Pengiriman> findByStatusAndMandorIdContainingIgnoreCaseAndCreatedAtBetween(
            PengirimanStatus status, String mandorSearch, Instant from, Instant to);
}
