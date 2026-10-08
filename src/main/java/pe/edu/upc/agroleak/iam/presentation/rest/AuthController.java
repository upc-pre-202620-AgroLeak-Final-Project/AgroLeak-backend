package pe.edu.upc.agroleak.iam.presentation.rest;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import pe.edu.upc.agroleak.iam.application.AuthService;
import pe.edu.upc.agroleak.iam.application.TokenIssuer;
import pe.edu.upc.agroleak.iam.presentation.rest.dto.*;

@RestController
@RequestMapping("/api/v1/iam/auth")
@Tag(name = "IAM Authentication")
@SecurityRequirements
public class AuthController {
    private final AuthService auth;
    private final TokenIssuer jwt;

    public AuthController(AuthService auth, TokenIssuer jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest r) {
        return UserResponse.from(auth.register(r.firstName(), r.lastName(), r.email(), r.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        var user = auth.login(r.email(), r.password());
        return new LoginResponse(jwt.issue(user), "Bearer", UserResponse.from(user));
    }
}
