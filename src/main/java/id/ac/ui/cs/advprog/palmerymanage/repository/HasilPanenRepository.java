package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.HasilPanen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface HasilPanenRepository extends JpaRepository<HasilPanen, UUID> {
}