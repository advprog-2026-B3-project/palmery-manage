package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HarvestResultRepository extends JpaRepository<HarvestResult, UUID> {

    //apakah buruh sudah input panen hari ini (Guard 1x sehari)
    boolean existsByWorkerIdAndHarvestDate(UUID workerId, LocalDate harvestDate);
    List<HarvestResult> findByWorkerId(UUID workerId);

    @Query("SELECT h FROM HarvestResult h WHERE h.workerId = :workerId " +
            "AND (cast(:startDate as date) IS NULL OR h.harvestDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR h.harvestDate <= :endDate) " +
            "AND (:status IS NULL OR h.status = :status)")
    List<HarvestResult> findBuruhHistory(
            @Param("workerId") UUID workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status
    );

    @Query("SELECT h FROM HarvestResult h WHERE " +
            "(cast(:date as date) IS NULL OR h.harvestDate = :date) " +
            "AND (:workerId IS NULL OR h.workerId = :workerId)")
    List<HarvestResult> findMandorHistory(
            @Param("date") LocalDate date,
            @Param("workerId") UUID workerId
    );
}