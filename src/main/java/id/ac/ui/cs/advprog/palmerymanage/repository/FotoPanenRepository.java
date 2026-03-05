package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FotoPanenRepository extends JpaRepository<HarvestPhoto, UUID> {
}