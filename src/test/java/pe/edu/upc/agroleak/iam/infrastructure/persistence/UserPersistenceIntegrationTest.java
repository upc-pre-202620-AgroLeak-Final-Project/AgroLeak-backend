package pe.edu.upc.agroleak.iam.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.iam.application.UserQueryService;
import pe.edu.upc.agroleak.iam.domain.model.Role;
import pe.edu.upc.agroleak.iam.domain.model.User;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserPersistenceIntegrationTest {
    private static final String HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Autowired UserRepository repository;
    @Autowired UserQueryService queryService;
    @Autowired EntityManager entityManager;

    private User persist(String email, Role role) {
        User saved = repository.save(new User("Kalid", "Palacios", email, HASH, role));
        entityManager.flush();
        entityManager.clear();
        return saved;
    }

    @Test
    void shouldPersistUserWithDefaultActiveAndTimestamps() {
        User saved = persist("persist@example.com", Role.FARMER);
        assertThat(saved.getId()).isNotNull();
        User loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getFirstName()).isEqualTo("Kalid");
        assertThat(loaded.getLastName()).isEqualTo("Palacios");
        assertThat(loaded.getEmail()).isEqualTo("persist@example.com");
        assertThat(loaded.getPasswordHash()).isEqualTo(HASH);
        assertThat(loaded.getRole()).isEqualTo(Role.FARMER);
        assertThat(loaded.isActive()).isTrue();
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isEqualTo(loaded.getCreatedAt());
    }

    @Test
    void shouldFindByEmailAndReportExistence() {
        User saved = persist("lookup@example.com", Role.TECHNICIAN);
        assertThat(repository.findByEmail("lookup@example.com")).get()
                .extracting(User::getId).isEqualTo(saved.getId());
        assertThat(repository.existsByEmail("lookup@example.com")).isTrue();
        assertThat(repository.findByEmail("missing@example.com")).isEmpty();
        assertThat(repository.existsByEmail("missing@example.com")).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void shouldStoreRoleAsString(Role role) {
        User saved = persist("role@example.com", role);
        Object storedRole = entityManager.createNativeQuery("select role from users where id = :id")
                .setParameter("id", saved.getId()).getSingleResult();
        assertThat(storedRole).isEqualTo(role.name());
        assertThat(repository.findById(saved.getId()).orElseThrow().getRole()).isEqualTo(role);
    }

    @Test
    void shouldRejectDuplicateEmailInDatabase() {
        persist("unique@example.com", Role.FARMER);
        repository.save(new User("Otro", "Usuario", "unique@example.com", HASH, Role.ADMIN));
        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    void shouldUpdateTimestampWithoutChangingCreationTime() {
        User saved = persist("timestamps@example.com", Role.FARMER);
        // Establish an earlier timestamp without sleeping or depending on clock precision.
        entityManager.createNativeQuery("update users set updated_at = created_at - interval '1' day where id = :id")
                .setParameter("id", saved.getId()).executeUpdate();
        User managed = repository.findById(saved.getId()).orElseThrow();
        Instant createdAt = managed.getCreatedAt();
        Instant oldUpdatedAt = managed.getUpdatedAt();
        managed.setActive(false);
        repository.save(managed);
        entityManager.flush();
        entityManager.clear();
        User reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.isActive()).isFalse();
        assertThat(reloaded.getCreatedAt()).isEqualTo(createdAt);
        assertThat(reloaded.getUpdatedAt()).isAfter(oldUpdatedAt);
        assertThat(reloaded.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }

    @Test
    void shouldQueryUsersThroughApplicationService() {
        User first = persist("first@example.com", Role.FARMER);
        User second = persist("second@example.com", Role.ADMIN);
        assertThat(queryService.getById(first.getId()).getEmail()).isEqualTo("first@example.com");
        assertThat(queryService.getByEmail("second@example.com").getId()).isEqualTo(second.getId());
        assertThat(queryService.findAll()).extracting(User::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldReportMissingUsers() {
        assertThatThrownBy(() -> queryService.getById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> queryService.getByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
