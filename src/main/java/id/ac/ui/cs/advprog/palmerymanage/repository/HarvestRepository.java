package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HarvestRepository extends JpaRepository<Harvest, String> {

    List<Harvest> findByKebunIdAndReadyForDeliveryIsTrue(String kebunId);
}

