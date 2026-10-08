package pe.edu.upc.agroleak.iam.infrastructure.security;

import java.time.Instant;
import java.time.Duration;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import pe.edu.upc.agroleak.iam.domain.model.User;

@Service
public class JwtService implements pe.edu.upc.agroleak.iam.application.TokenIssuer {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long expirationMs;

    public JwtService(@Value("${agroleak.jwt.secret:}") String secret,
                      @Value("${agroleak.jwt.expiration-ms:3600000}") long expirationMs) {
        byte[] bytes;
        if (secret.isBlank()) {
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET requiere al menos 32 bytes");
        }
        if (expirationMs < 1000) throw new IllegalArgumentException("JWT_EXPIRATION_MS debe ser al menos 1000");
        this.expirationMs = expirationMs;
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtDecoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator("agroleak")));
        decoder = jwtDecoder;
    }

    public String issue(User user) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("agroleak").subject(user.getId().toString())
                .claim("email", user.getEmail()).claim("role", user.getRole().name())
                .issuedAt(now).expiresAt(now.plusMillis(expirationMs)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        return decoder.decode(token);
    }
}
