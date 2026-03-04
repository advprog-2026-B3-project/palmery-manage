package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, String> {

    List<Driver> findByKebunIdAndNamaContainingIgnoreCase(String kebunId, String nama);
}

