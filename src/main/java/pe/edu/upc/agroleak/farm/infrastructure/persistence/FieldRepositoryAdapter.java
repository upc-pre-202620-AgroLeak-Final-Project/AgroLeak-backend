package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upc.agroleak.farm.domain.model.Field;
import pe.edu.upc.agroleak.farm.domain.repository.FieldRepository;

import java.util.*;

@Repository
public class FieldRepositoryAdapter implements FieldRepository {
    private final JpaFieldRepository repository;

    public FieldRepositoryAdapter(JpaFieldRepository repository) {
        this.repository = repository;
    }

    public Field save(Field entity) {
        return repository.save(entity);
    }

    public Optional<Field> findById(UUID id) {
        return repository.findById(id);
    }

    public List<Field> findByFarmId(UUID id) {
        return repository.findByFarmId(id);
    }

    public boolean existsByFarmId(UUID id) {
        return repository.existsByFarmId(id);
    }

    public void delete(Field entity) {
        repository.delete(entity);
    }
}
