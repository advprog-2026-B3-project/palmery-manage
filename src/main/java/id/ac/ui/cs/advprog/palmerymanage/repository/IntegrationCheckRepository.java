package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntegrationCheckRepository extends JpaRepository<IntegrationCheck, Long> {
    long countBySource(String source);

    List<IntegrationCheck> findTop10ByOrderByCreatedAtDesc();
}
