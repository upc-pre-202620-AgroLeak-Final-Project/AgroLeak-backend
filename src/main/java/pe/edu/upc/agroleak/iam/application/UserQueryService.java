package pe.edu.upc.agroleak.iam.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.iam.domain.model.User;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserRepository repository;

    public UserQueryService(UserRepository repository) {
        this.repository = repository;
    }

    public User getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    public User getByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public List<User> findAll() { return repository.findAll(); }
}
