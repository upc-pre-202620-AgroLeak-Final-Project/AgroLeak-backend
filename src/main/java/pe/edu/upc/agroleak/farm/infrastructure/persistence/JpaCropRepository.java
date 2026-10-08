package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.farm.domain.model.Crop;

import java.util.*;

public interface JpaCropRepository extends JpaRepository<Crop, UUID> {
    List<Crop> findBySectorId(UUID id);

    boolean existsBySectorId(UUID id);
}
