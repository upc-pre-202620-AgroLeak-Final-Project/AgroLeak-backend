package pe.edu.upc.agroleak.iam.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import pe.edu.upc.agroleak.common.exception.BusinessRuleException;
import pe.edu.upc.agroleak.iam.domain.model.*;
import pe.edu.upc.agroleak.iam.domain.repository.UserRepository;

import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String password) {
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La contraseña excede 72 bytes UTF-8");
        if (users.existsByEmail(normalized)) throw new BusinessRuleException("El email ya está registrado");
        return users.save(new User(firstName.strip(), lastName.strip(), normalized, encoder.encode(password), Role.FARMER));
    }

    @Transactional(readOnly = true)
    public User login(String email, String password) {
        var user = users.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!user.isActive() || password.getBytes(StandardCharsets.UTF_8).length > 72 || !encoder.matches(password, user.getPasswordHash()))
            throw new BadCredentialsException("Credenciales inválidas");
        return user;
    }
}
