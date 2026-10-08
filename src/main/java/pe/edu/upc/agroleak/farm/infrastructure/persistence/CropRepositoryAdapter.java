package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upc.agroleak.farm.domain.model.Crop;
import pe.edu.upc.agroleak.farm.domain.repository.CropRepository;

import java.util.*;

@Repository
public class CropRepositoryAdapter implements CropRepository {
    private final JpaCropRepository repository;

    public CropRepositoryAdapter(JpaCropRepository repository) {
        this.repository = repository;
    }

    public Crop save(Crop entity) {
        return repository.save(entity);
    }

    public Optional<Crop> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Crop> findBySectorId(UUID id) {
        return repository.findBySectorId(id);
    }

    public boolean existsBySectorId(UUID id) {
        return repository.existsBySectorId(id);
    }

    public void delete(Crop entity) {
        repository.delete(entity);
    }
}
