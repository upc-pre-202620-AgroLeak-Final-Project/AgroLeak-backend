package pe.edu.upc.agroleak.production;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import pe.edu.upc.agroleak.demo.DemoDataLoader;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;
import pe.edu.upc.agroleak.farm.infrastructure.persistence.JpaFarmRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:agroleak_demo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=VALUE",
    "agroleak.demo-data-enabled=true","agroleak.demo.password=DemoTest123!"})
@ActiveProfiles("test")
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class DemoIntegrationTest {
    @Autowired DemoDataLoader loader; @Autowired UserRepository users; @Autowired DeviceRepository devices;
    @Autowired JpaFarmRepository farms; @Autowired PasswordEncoder encoder;
    @Autowired pe.edu.upc.agroleak.demo.DemoScenarioService scenarios;
    @Autowired pe.edu.upc.agroleak.alerts.application.AlertService alerts;
    @Test
    @org.springframework.transaction.annotation.Transactional
    void switchingBackToNormalDoesNotCreateMixedSnapshotAlerts() {
        var user=users.findByEmail("demo@agroleak.local").orElseThrow();
        var old=org.springframework.security.core.context.SecurityContextHolder.getContext();
        var context=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user.getId().toString(),null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_FARMER"))));
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        try {
            var gateway=devices.findAll().stream().filter(d -> d.getName().equals("Demo Gateway")).findFirst().orElseThrow();
            scenarios.obstruction(gateway.getId());
            var incidents=alerts.findByDevice(gateway.getId(),pe.edu.upc.agroleak.alerts.domain.model.AlertStatus.ACTIVE);
            assertThat(incidents).isNotEmpty();
            incidents.forEach(a -> alerts.resolve(a.getId()));
            scenarios.normal(gateway.getId());
            assertThat(alerts.activeCount(gateway.getId())).isZero();
        } finally { org.springframework.security.core.context.SecurityContextHolder.setContext(old); }
    }
    @Test void seedsCompleteHierarchyWithEncodedPasswordAndIsIdempotent() {
        var user=users.findByEmail("demo@agroleak.local").orElseThrow();
        assertThat(encoder.matches("DemoTest123!",user.getPasswordHash())).isTrue();
        assertThat(farms.count()).isEqualTo(1);assertThat(devices.count()).isEqualTo(6);
        loader.run();
        assertThat(farms.count()).isEqualTo(1);assertThat(devices.count()).isEqualTo(6);
        assertThat(devices.findAll()).allMatch(d -> d.getSectorId()!=null && d.getLastSeen()!=null);
    }
}
