package pe.edu.upc.agroleak.farm.domain.repository;

import pe.edu.upc.agroleak.farm.domain.model.Crop;

import java.util.*;

public interface CropRepository {
    Crop save(Crop entity);

    Optional<Crop> findById(UUID id);

    List<Crop> findBySectorId(UUID id);

    boolean existsBySectorId(UUID id);

    void delete(Crop entity);
}
