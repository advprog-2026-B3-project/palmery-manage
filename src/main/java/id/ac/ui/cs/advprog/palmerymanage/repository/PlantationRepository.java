package id.ac.ui.cs.advprog.palmerymanage.repository;

import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlantationRepository extends JpaRepository<Plantation, UUID> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    Optional<Plantation> findByCode(String code);

    @Query("""
            SELECT p FROM Plantation p
            WHERE (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (COALESCE(:code, '') = '' OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%')))
            """)
    List<Plantation> findAllByFilter(
            @Param("name") String name,
            @Param("code") String code
    );

    List<Plantation> findAllByIdNot(UUID excludeId);
}
