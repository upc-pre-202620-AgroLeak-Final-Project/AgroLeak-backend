package pe.edu.upc.agroleak.production;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.edu.upc.agroleak.iam.domain.model.*;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;
import java.sql.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

/** Opt-in real PostgreSQL test. Creates/drops only randomly named test schemas. */
@SpringBootTest(properties={"spring.flyway.enabled=true","spring.jpa.hibernate.ddl-auto=validate"})
@ActiveProfiles("test")
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
@EnabledIfEnvironmentVariable(named="AGROLEAK_TEST_POSTGRES_URL",matches="jdbc:postgresql:.*")
class PostgresMigrationIntegrationTest {
    static final String BASE=System.getenv("AGROLEAK_TEST_POSTGRES_URL");
    static final String USER=System.getenv().getOrDefault("AGROLEAK_TEST_POSTGRES_USERNAME","agroleak");
    static final String PASSWORD=System.getenv().getOrDefault("AGROLEAK_TEST_POSTGRES_PASSWORD","agroleak");
    static final String SCHEMA="agroleak_test_"+UUID.randomUUID().toString().replace("-","");
    static String url(String schema) { return BASE+(BASE.contains("?") ? "&" : "?")+"currentSchema="+schema; }
    @DynamicPropertySource static void properties(DynamicPropertyRegistry p) {
        p.add("spring.datasource.url",() -> url(SCHEMA));p.add("spring.datasource.username",() -> USER);
        p.add("spring.datasource.password",() -> PASSWORD);p.add("spring.datasource.driver-class-name",() -> "org.postgresql.Driver");
        p.add("spring.flyway.schemas",() -> SCHEMA);p.add("spring.flyway.default-schema",() -> SCHEMA);
    }
    @Autowired JdbcTemplate jdbc; @Autowired UserRepository users;
    @Test void freshMigrationsMatchJpaAndPersistUsers() {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success and type='SQL'",Integer.class)).isEqualTo(3);
        User saved=users.save(new User("Postgres","Test","postgres@example.com","test-hash",Role.FARMER));
        assertThat(users.findById(saved.getId())).isPresent();
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns where table_schema=? and table_name='alerts' and column_name='acknowledged_at'",Integer.class,SCHEMA)).isEqualTo(1);
    }
    @Test void legacyHibernateSchemaBaselinesWithoutLosingRows() throws Exception {
        String schema="agroleak_upgrade_"+UUID.randomUUID().toString().replace("-","");
        try {
            Flyway.configure().dataSource(BASE,USER,PASSWORD).schemas(schema).defaultSchema(schema).target("1").load().migrate();
            try(Connection c=DriverManager.getConnection(url(schema),USER,PASSWORD);Statement s=c.createStatement()) {
                s.execute("insert into devices(id,name,location,status) values ('00000000-0000-0000-0000-000000000001','Legacy','Ica','OFFLINE')");
                s.execute("alter table alerts add constraint alerts_type_check check(type in ('LEAK','OBSTRUCTION','PRESSURE_OUT_OF_RANGE'))");
                s.execute("alter table alerts add constraint alerts_status_check check(status in ('ACTIVE','RESOLVED'))");
                s.execute("alter table alerts add constraint alerts_severity_check check(severity in ('LOW','MEDIUM','HIGH'))");
                s.execute("insert into alerts(id,device_id,type,severity,message,status,created_at) values ('00000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000001','PRESSURE_OUT_OF_RANGE','MEDIUM','Legacy','ACTIVE',now())");
                // Simulate the preceding Hibernate-only schema, without Flyway history.
                s.execute("drop table flyway_schema_history");
            }
            var flyway=Flyway.configure().dataSource(BASE,USER,PASSWORD).schemas(schema).defaultSchema(schema)
                    .baselineOnMigrate(true).baselineVersion("0").load();
            assertThat(flyway.migrate().migrationsExecuted).isEqualTo(3);
            assertThat(flyway.migrate().migrationsExecuted).isZero();
            try(Connection c=DriverManager.getConnection(url(schema),USER,PASSWORD);Statement s=c.createStatement()) {
                try(ResultSet rows=s.executeQuery("select type from alerts")) { assertThat(rows.next()).isTrue();assertThat(rows.getString(1)).isEqualTo("PRESSURE_OUT_OF_RANGE"); }
                s.execute("update alerts set status='ACKNOWLEDGED',severity='CRITICAL',type='LOW_PRESSURE'");
            }
        } finally { drop(schema); }
    }
    @AfterAll static void cleanup() throws Exception { drop(SCHEMA); }
    static void drop(String schema) throws Exception {
        if(!schema.matches("agroleak_(test|upgrade)_[a-f0-9]+")) throw new IllegalArgumentException("Not a test schema");
        try(Connection c=DriverManager.getConnection(BASE,USER,PASSWORD);Statement s=c.createStatement()) { s.execute("drop schema if exists "+schema+" cascade"); }
    }
}
