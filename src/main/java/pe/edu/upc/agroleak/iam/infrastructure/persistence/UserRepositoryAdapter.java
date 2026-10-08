package pe.edu.upc.agroleak.iam.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import pe.edu.upc.agroleak.iam.domain.model.User;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final JpaUserRepository repository;

    public UserRepositoryAdapter(JpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) { return repository.save(user); }

    @Override
    public Optional<User> findById(UUID id) { return repository.findById(id); }

    @Override
    public Optional<User> findByEmail(String email) { return repository.findByEmail(email); }

    @Override
    public boolean existsByEmail(String email) { return repository.existsByEmail(email); }

    @Override
    public List<User> findAll() { return repository.findAll(); }
}
