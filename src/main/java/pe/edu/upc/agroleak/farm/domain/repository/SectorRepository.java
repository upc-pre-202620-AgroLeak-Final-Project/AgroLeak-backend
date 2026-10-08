package pe.edu.upc.agroleak.farm.domain.repository;

import pe.edu.upc.agroleak.farm.domain.model.Sector;

import java.util.*;

public interface SectorRepository {
    List<Sector> findAll();
    Optional<Sector> findLockedById(UUID id);
    Sector save(Sector entity);

    Optional<Sector> findById(UUID id);

    List<Sector> findByFieldId(UUID id);

    boolean existsByFieldId(UUID id);

    void delete(Sector entity);
}
