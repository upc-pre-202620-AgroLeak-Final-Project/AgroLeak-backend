package pe.edu.upc.agroleak.iam.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import pe.edu.upc.agroleak.iam.application.CurrentUser;

import java.util.UUID;

@Component
public class SecurityCurrentUser implements CurrentUser {
    public UUID id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("Autenticación requerida");
        return UUID.fromString(auth.getName());
    }

    private boolean role(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAdmin() {
        return role("ADMIN");
    }

    public boolean isTechnician() {
        return role("TECHNICIAN");
    }
}
