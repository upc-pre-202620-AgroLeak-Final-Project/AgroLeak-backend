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
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class DemoIntegrationTest {
    @Autowired DemoDataLoader loader; @Autowired UserRepository users; @Autowired DeviceRepository devices;
    @Autowired JpaFarmRepository farms; @Autowired PasswordEncoder encoder;
    @Autowired pe.edu.upc.agroleak.demo.DemoScenarioService scenarios;
    @Autowired pe.edu.upc.agroleak.alerts.application.AlertService alerts;
    @Autowired pe.edu.upc.agroleak.demo.PresentationDataLoader presentation;
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
    @Autowired pe.edu.upc.agroleak.monitoring.infrastructure.SensorReadingRepository readings;
    @Autowired pe.edu.upc.agroleak.alerts.infrastructure.AlertRepository alertRows;
    @Autowired pe.edu.upc.agroleak.pests.infrastructure.PestObservationRepository pestRows;
    @Autowired pe.edu.upc.agroleak.irrigation.infrastructure.ValveCommandRepository commands;

    @Test void masterCanLoginReadDashboardAndRestartWithoutDuplicatingData() throws Exception {
        var user=users.findByEmail("maestro@agroleak.local").orElseThrow();
        assertThat(user.getRole()).isEqualTo(pe.edu.upc.agroleak.iam.domain.model.Role.ADMIN);
        assertThat(encoder.matches("123456789",user.getPasswordHash())).isTrue();
        var login=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/v1/iam/auth/login").contentType("application/json")
                .content("{\"email\":\"maestro@agroleak.local\",\"password\":\"123456789\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();
        String token=json.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
        for(String path: java.util.List.of("/api/v1/iam/users/me", "/api/v1/analytics/dashboard",
                "/api/v1/analytics/charts", "/api/v1/farms", "/api/v1/devices", "/api/v1/alerts")) {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path)
                    .header("Authorization","Bearer "+token))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        }
        assertThat(alertRows.findAll()).extracting(a -> a.getStatus()).contains(
                pe.edu.upc.agroleak.alerts.domain.model.AlertStatus.ACTIVE,
                pe.edu.upc.agroleak.alerts.domain.model.AlertStatus.ACKNOWLEDGED,
                pe.edu.upc.agroleak.alerts.domain.model.AlertStatus.RESOLVED);
        long readingCount=readings.count(), alertCount=alertRows.count();
        assertThat(readingCount).isGreaterThan(8000);
        assertThat(pestRows.count()).isEqualTo(8);
        assertThat(commands.count()).isEqualTo(7);
        presentation.run();
        assertThat(readings.count()).isEqualTo(readingCount);
        assertThat(alertRows.count()).isEqualTo(alertCount);
        assertThat(pestRows.count()).isEqualTo(8);
        assertThat(commands.count()).isEqualTo(7);
        assertThat(farms.count()).isEqualTo(2);
        assertThat(devices.count()).isEqualTo(12);
        assertThat(users.findByEmail(user.getEmail()).orElseThrow().getPasswordHash()).isEqualTo(user.getPasswordHash());
    }
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
        assertThat(farms.count()).isEqualTo(2);assertThat(devices.count()).isEqualTo(12);
        loader.run();
        assertThat(farms.count()).isEqualTo(2);assertThat(devices.count()).isEqualTo(12);
        assertThat(devices.findAll()).allMatch(d -> d.getSectorId()!=null && d.getLastSeen()!=null);
    }
}
