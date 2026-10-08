package pe.edu.upc.agroleak.iam.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
    private static final String HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    static Stream<Object[]> invalidUsers() {
        return Stream.of(
                new Object[]{null, "Palacios", "user@example.com", HASH, Role.FARMER},
                new Object[]{" ", "Palacios", "user@example.com", HASH, Role.FARMER},
                new Object[]{"Kalid", null, "user@example.com", HASH, Role.FARMER},
                new Object[]{"Kalid", " ", "user@example.com", HASH, Role.FARMER},
                new Object[]{"Kalid", "Palacios", null, HASH, Role.FARMER},
                new Object[]{"Kalid", "Palacios", " ", HASH, Role.FARMER},
                new Object[]{"Kalid", "Palacios", "user@example.com", null, Role.FARMER},
                new Object[]{"Kalid", "Palacios", "user@example.com", " ", Role.FARMER},
                new Object[]{"Kalid", "Palacios", "user@example.com", HASH, null},
                new Object[]{"x".repeat(101), "Palacios", "user@example.com", HASH, Role.FARMER},
                new Object[]{"Kalid", "x".repeat(101), "user@example.com", HASH, Role.FARMER},
                new Object[]{"Kalid", "Palacios", "x".repeat(255), HASH, Role.FARMER},
                new Object[]{"Kalid", "Palacios", "user@example.com", "x".repeat(256), Role.FARMER});
    }

    @ParameterizedTest
    @MethodSource("invalidUsers")
    void shouldRejectInvalidRequiredFields(String firstName, String lastName, String email,
                                           String passwordHash, Role role) {
        assertThatThrownBy(() -> new User(firstName, lastName, email, passwordHash, role))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
