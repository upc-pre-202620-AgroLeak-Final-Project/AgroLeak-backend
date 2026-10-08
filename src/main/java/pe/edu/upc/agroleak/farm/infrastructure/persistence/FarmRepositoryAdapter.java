package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upc.agroleak.farm.domain.model.Farm;
import pe.edu.upc.agroleak.farm.domain.repository.FarmRepository;

import java.util.*;

@Repository
public class FarmRepositoryAdapter implements FarmRepository {
    private final JpaFarmRepository repository;

    public FarmRepositoryAdapter(JpaFarmRepository repository) {
        this.repository = repository;
    }

    public Farm save(Farm entity) {
        return repository.save(entity);
    }

    public Optional<Farm> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Farm> findByOwnerId(UUID id) {
        return repository.findByOwnerId(id);
    }

    public boolean existsByOwnerId(UUID id) {
        return repository.existsByOwnerId(id);
    }

    public void delete(Farm entity) {
        repository.delete(entity);
    }

    public List<Farm> findAll() {
        return repository.findAll();
    }
}
