package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.farm.domain.model.Field;

import java.util.*;

public interface JpaFieldRepository extends JpaRepository<Field, UUID> {
    List<Field> findByFarmId(UUID id);

    boolean existsByFarmId(UUID id);
}
