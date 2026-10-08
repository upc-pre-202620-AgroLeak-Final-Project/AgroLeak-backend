package pe.edu.upc.agroleak.farm.domain.repository;

import pe.edu.upc.agroleak.farm.domain.model.Field;

import java.util.*;

public interface FieldRepository {
    Field save(Field entity);

    Optional<Field> findById(UUID id);

    List<Field> findByFarmId(UUID id);

    boolean existsByFarmId(UUID id);

    void delete(Field entity);
}
