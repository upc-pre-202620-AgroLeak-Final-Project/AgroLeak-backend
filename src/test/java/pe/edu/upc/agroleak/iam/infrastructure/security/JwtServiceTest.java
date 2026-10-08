package pe.edu.upc.agroleak.iam.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "test-only-secret-with-at-least-32-bytes";

    private String token(Instant expires, String secret) {
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        var claims = JwtClaimsSet.builder().issuer("agroleak").subject("00000000-0000-0000-0000-000000000001")
                .issuedAt(expires.minusSeconds(60)).expiresAt(expires).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @Test
    void rejectsExpiredOrIncorrectlySignedTokens() {
        var service = new JwtService(SECRET, 3600000);
        assertThatThrownBy(() -> service.decode(token(Instant.now().minusSeconds(1), SECRET))).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> service.decode(token(Instant.now().plusSeconds(60), SECRET + "other"))).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsWeakSecretAndInvalidExpiration() {
        assertThatThrownBy(() -> new JwtService("weak", 3600000)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtService(SECRET, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
