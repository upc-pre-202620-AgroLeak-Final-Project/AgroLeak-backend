package pe.edu.upc.agroleak.farm.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.farm.domain.model.Sector;

import java.util.*;

public interface JpaSectorRepository extends JpaRepository<Sector, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from Sector s where s.id = :id")
    Optional<Sector> findLockedById(UUID id);
    List<Sector> findByFieldId(UUID id);

    boolean existsByFieldId(UUID id);
}
