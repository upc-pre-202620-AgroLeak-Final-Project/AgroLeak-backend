package pe.edu.upc.agroleak.farm.domain.repository;

import pe.edu.upc.agroleak.farm.domain.model.Farm;

import java.util.*;

public interface FarmRepository {
    Farm save(Farm entity);

    Optional<Farm> findById(UUID id);

    List<Farm> findByOwnerId(UUID id);

    boolean existsByOwnerId(UUID id);

    void delete(Farm entity);

    List<Farm> findAll();
}
