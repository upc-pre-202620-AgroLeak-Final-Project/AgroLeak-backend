package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.farm.domain.model.Farm;

import java.util.*;

public interface JpaFarmRepository extends JpaRepository<Farm, UUID> {
    List<Farm> findByOwnerId(UUID id);

    boolean existsByOwnerId(UUID id);
}
