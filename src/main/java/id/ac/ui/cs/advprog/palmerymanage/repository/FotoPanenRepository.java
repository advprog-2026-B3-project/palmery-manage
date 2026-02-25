package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.FotoPanen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FotoPanenRepository extends JpaRepository<FotoPanen, UUID> {
}