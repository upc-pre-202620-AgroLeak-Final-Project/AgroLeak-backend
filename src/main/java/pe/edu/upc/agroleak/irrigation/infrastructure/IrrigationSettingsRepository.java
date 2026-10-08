package pe.edu.upc.agroleak.irrigation.infrastructure;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.irrigation.domain.model.IrrigationSettings;
import java.util.UUID;
public interface IrrigationSettingsRepository extends JpaRepository<IrrigationSettings,UUID> {}
