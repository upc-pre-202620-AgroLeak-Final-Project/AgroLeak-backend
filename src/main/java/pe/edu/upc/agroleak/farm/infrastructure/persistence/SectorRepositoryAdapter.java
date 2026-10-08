package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upc.agroleak.farm.domain.model.Sector;
import pe.edu.upc.agroleak.farm.domain.repository.SectorRepository;

import java.util.*;

@Repository
public class SectorRepositoryAdapter implements SectorRepository {
    private final JpaSectorRepository repository;

    public SectorRepositoryAdapter(JpaSectorRepository repository) {
        this.repository = repository;
    }

    public java.util.Optional<Sector> findLockedById(UUID id) { return repository.findLockedById(id); }
    public java.util.List<Sector> findAll() { return repository.findAll(); }

    public Sector save(Sector entity) {
        return repository.save(entity);
    }

    public Optional<Sector> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Sector> findByFieldId(UUID id) {
        return repository.findByFieldId(id);
    }

    public boolean existsByFieldId(UUID id) {
        return repository.existsByFieldId(id);
    }

    public void delete(Sector entity) {
        repository.delete(entity);
    }
}
