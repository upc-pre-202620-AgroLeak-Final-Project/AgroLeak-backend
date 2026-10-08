package pe.edu.upc.agroleak.iam.presentation.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.iam.domain.model.*;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import pe.edu.upc.agroleak.iam.infrastructure.security.JwtService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class IamFarmIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository users;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtService jwt;
    @Autowired
    DeviceRepository devices;
    private static final String PASSWORD = "Secret123!";

    private JsonNode call(MockHttpServletRequestBuilder request, String token, Object body, int status) throws Exception {
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
        String content = mvc.perform(request).andExpect(status().is(status)).andReturn().getResponse().getContentAsString();
        return content.isEmpty() ? mapper.nullNode() : mapper.readTree(content);
    }

    private JsonNode register(String email) throws Exception {
        return call(post("/api/v1/iam/auth/register"), null,
                Map.of("firstName", "Kalid", "lastName", "Palacios", "email", email, "password", PASSWORD), 201);
    }

    private String login(String email) throws Exception {
        return call(post("/api/v1/iam/auth/login"), null, Map.of("email", email, "password", PASSWORD), 200).path("accessToken").asText();
    }

    private String farmer(String email) throws Exception {
        register(email);
        return login(email);
    }

    private String role(Role role) {
        return jwt.issue(users.save(new User("Test", "Role", role.name() + "@example.com", encoder.encode(PASSWORD), role)));
    }

    private JsonNode farm(String token) throws Exception {
        return call(post("/api/v1/farms"), token, Map.of("name", "Farm A", "location", "Lima", "areaHectares", 10), 201);
    }

    private String[] hierarchy(String token) throws Exception {
        String farm = farm(token).path("id").asText();
        String field = call(post("/api/v1/farms/" + farm + "/fields"), token,
                Map.of("name", "Field A", "areaHectares", 5, "description", "Test"), 201).path("id").asText();
        String sector = call(post("/api/v1/fields/" + field + "/sectors"), token,
                Map.of("name", "Sector A", "areaHectares", 2, "status", "ACTIVE"), 201).path("id").asText();
        String crop = call(post("/api/v1/sectors/" + sector + "/crops"), token,
                Map.of("name", "Tomate", "variety", "Cherry", "plantedAt", "2026-01-01", "status", "PLANTED"), 201).path("id").asText();
        return new String[]{farm, field, sector, crop};
    }

    @Test
    void registerHashesPasswordAndDefaultsToActiveFarmer() throws Exception {
        var response = register("KALID@example.com");
        assertThat(response.path("role").asText()).isEqualTo("FARMER");
        assertThat(response.has("password")).isFalse();
        assertThat(response.has("passwordHash")).isFalse();
        User stored = users.findByEmail("kalid@example.com").orElseThrow();
        assertThat(stored.isActive()).isTrue();
        assertThat(stored.getPasswordHash()).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, stored.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsDuplicateEmailAndInvalidRegistration() throws Exception {
        register("kalid@example.com");
        call(post("/api/v1/iam/auth/register"), null,
                Map.of("firstName", "K", "lastName", "P", "email", "KALID@example.com", "password", PASSWORD), 409);
        var invalid = call(post("/api/v1/iam/auth/register"), null,
                Map.of("firstName", "", "lastName", "", "email", "invalid", "password", "short"), 400);
        assertThat(invalid.path("validationErrors").size()).isEqualTo(4);
    }

    @Test
    void loginIssuesJwtAndMeReturnsCurrentUser() throws Exception {
        var registered = register("kalid@example.com");
        var response = call(post("/api/v1/iam/auth/login"), null, Map.of("email", "KALID@example.com", "password", PASSWORD), 200);
        assertThat(response.path("tokenType").asText()).isEqualTo("Bearer");
        assertThat(response.path("user")).isEqualTo(registered);
        String token = response.path("accessToken").asText();
        var decoded = jwt.decode(token);
        assertThat(decoded.getSubject()).isEqualTo(registered.path("id").asText());
        assertThat(decoded.getClaimAsString("email")).isEqualTo("kalid@example.com");
        assertThat(decoded.getClaimAsString("role")).isEqualTo("FARMER");
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
        assertThat(call(get("/api/v1/iam/users/me"), token, null, 200)).isEqualTo(registered);
    }

    @Test
    void rejectsWrongCredentialsInactiveUsersAndInvalidTokens() throws Exception {
        String token = farmer("kalid@example.com");
        call(post("/api/v1/iam/auth/login"), null, Map.of("email", "kalid@example.com", "password", "Wrong123!"), 401);
        call(post("/api/v1/iam/auth/login"), null, Map.of("email", "unknown@example.com", "password", PASSWORD), 401);
        call(get("/api/v1/iam/users/me"), "invalid.jwt.token", null, 401);
        call(get("/api/v1/iam/users/me"), null, null, 401);
        User user = users.findByEmail("kalid@example.com").orElseThrow();
        user.setActive(false);
        users.save(user);
        call(get("/api/v1/iam/users/me"), token, null, 401);
        call(post("/api/v1/iam/auth/login"), null, Map.of("email", "kalid@example.com", "password", PASSWORD), 401);
    }

    @Test
    void listsOnlyOwnFarmsAndAllowsAdminGlobalAccess() throws Exception {
        String a = farmer("a@example.com"), b = farmer("b@example.com");
        var farmA = farm(a);
        farm(b);
        assertThat(farmA.path("ownerId").asText()).isEqualTo(users.findByEmail("a@example.com").orElseThrow().getId().toString());
        var list = call(get("/api/v1/farms"), a, null, 200);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(farmA);
        call(get("/api/v1/farms/" + farmA.path("id").asText()), b, null, 403);
        assertThat(call(get("/api/v1/farms"), role(Role.ADMIN), null, 200).size()).isEqualTo(2);
    }

    @Test
    void createsReadsUpdatesAndDeletesFarmHierarchy() throws Exception {
        String token = farmer("a@example.com");
        String[] ids = hierarchy(token);
        String[] kinds = {"farms", "fields", "sectors", "crops"};
        for (int i = 0; i < 4; i++)
            assertThat(call(get("/api/v1/" + kinds[i] + "/" + ids[i]), token, null, 200).path("id").asText()).isEqualTo(ids[i]);
        for (int i = 0; i < 3; i++)
            assertThat(call(get("/api/v1/" + kinds[i] + "/" + ids[i] + "/" + kinds[i + 1]), token, null, 200).size()).isEqualTo(1);
        call(put("/api/v1/farms/" + ids[0]), token, Map.of("name", "Updated", "location", "Ica", "areaHectares", 20), 200);
        call(put("/api/v1/fields/" + ids[1]), token, Map.of("name", "Updated", "areaHectares", 6), 200);
        call(put("/api/v1/sectors/" + ids[2]), token, Map.of("name", "Updated", "areaHectares", 3, "status", "MAINTENANCE"), 200);
        call(put("/api/v1/crops/" + ids[3]), token, Map.of("name", "Updated", "plantedAt", "2026-01-01", "status", "GROWING"), 200);
        call(delete("/api/v1/farms/" + ids[0]), token, null, 409);
        call(delete("/api/v1/fields/" + ids[1]), token, null, 409);
        call(delete("/api/v1/sectors/" + ids[2]), token, null, 409);
        for (int i = 3; i >= 0; i--) call(delete("/api/v1/" + kinds[i] + "/" + ids[i]), token, null, 204);
        call(get("/api/v1/farms/" + ids[0]), token, null, 404);
    }

    @Test
    void rejectsCrossOwnerHierarchyReadsAndWrites() throws Exception {
        String a = farmer("a@example.com"), b = farmer("b@example.com");
        String[] ids = hierarchy(a);
        String[] kinds = {"farms", "fields", "sectors", "crops"};
        for (int i = 0; i < 4; i++) {
            call(get("/api/v1/" + kinds[i] + "/" + ids[i]), b, null, 403);
            call(delete("/api/v1/" + kinds[i] + "/" + ids[i]), b, null, 403);
        }
        call(post("/api/v1/farms/" + ids[0] + "/fields"), b, Map.of("name", "Intruder", "areaHectares", 1), 403);
        call(post("/api/v1/fields/" + ids[1] + "/sectors"), b, Map.of("name", "Intruder", "areaHectares", 1, "status", "ACTIVE"), 403);
        call(post("/api/v1/sectors/" + ids[2] + "/crops"), b, Map.of("name", "Intruder", "plantedAt", "2026-01-01", "status", "PLANTED"), 403);
    }

    @Test
    void assignsDeviceToOwnedSectorAndProtectsIndirectRoutes() throws Exception {
        String a = farmer("a@example.com"), b = farmer("b@example.com");
        String[] ids = hierarchy(a), other = hierarchy(b);
        String device = call(post("/api/v1/devices"), a, Map.of("name", "Sensor", "location", "Lima", "deviceType", "FLOW_SENSOR", "batteryLevel", 75, "firmwareVersion", "1.0", "installationDate", "2026-01-01"), 201).path("id").asText();
        var assigned = call(patch("/api/v1/devices/" + device + "/sector"), a, Map.of("sectorId", ids[2]), 200);
        assertThat(assigned.path("sectorId").asText()).isEqualTo(ids[2]);
        assertThat(devices.findById(UUID.fromString(device)).orElseThrow().getSectorId()).isEqualTo(UUID.fromString(ids[2]));
        call(patch("/api/v1/devices/" + device + "/sector"), a, Map.of("sectorId", other[2]), 403);
        call(patch("/api/v1/devices/" + device + "/sector"), b, Map.of("sectorId", other[2]), 403);
        call(get("/api/v1/devices/" + device), b, null, 403);
        assertThat(call(get("/api/v1/devices"), b, null, 200).size()).isZero();
        for (String route : List.of("/readings/device/", "/pest-observations/device/", "/dashboard/"))
            call(get("/api/v1" + route + device), b, null, 403);
        call(get("/api/v1/alerts").param("deviceId", device), b, null, 403);
        call(get("/api/v1/valves/" + device + "/latest"), b, null, 403);
        call(delete("/api/v1/crops/" + ids[3]), a, null, 204);
        call(delete("/api/v1/sectors/" + ids[2]), a, null, 409);
        String tech = role(Role.TECHNICIAN);
        call(get("/api/v1/devices/" + device), tech, null, 200);
        call(get("/api/v1/readings/device/" + device), tech, null, 200);
        call(patch("/api/v1/devices/" + device + "/sector"), tech, Map.of("sectorId", ids[2]), 403);
        call(post("/api/v1/farms"), tech, Map.of("name", "Denied", "location", "Lima", "areaHectares", 1), 403);
    }

    @Test
    void preservesLegacyDevicesAndOnlyAdminCanClaimThem() throws Exception {
        Device legacy = devices.save(new Device("Legacy", "Lima"));
        String farmer = farmer("a@example.com"), admin = role(Role.ADMIN);
        String[] ids = hierarchy(farmer);
        call(get("/api/v1/devices/" + legacy.getId()), farmer, null, 403);
        call(patch("/api/v1/devices/" + legacy.getId() + "/sector"), farmer, Map.of("sectorId", ids[2]), 403);
        call(patch("/api/v1/devices/" + legacy.getId() + "/sector"), admin, Map.of("sectorId", ids[2]), 200);
        call(get("/api/v1/devices/" + legacy.getId()), farmer, null, 200);
        call(post("/api/v1/devices"), farmer, Map.of("name", "Legacy payload", "location", "Lima"), 201);
    }

    @Test
    void validatesReferencesAndAreasAndSupportsCors() throws Exception {
        String a = farmer("a@example.com");
        call(post("/api/v1/farms"), a, Map.of("name", "Bad", "location", "Lima", "areaHectares", -1), 400);
        call(post("/api/v1/farms/" + UUID.randomUUID() + "/fields"), a, Map.of("name", "Orphan", "areaHectares", 1), 404);
        call(post("/api/v1/devices"), a, Map.of("name", "Bad", "location", "Lima", "sectorId", UUID.randomUUID()), 404);
        mvc.perform(options("/api/v1/farms").header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        call(get("/api/v1/farms"), null, null, 401);
        call(get("/api/v1/devices"), null, null, 401);
    }
}
