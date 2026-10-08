package pe.edu.upc.agroleak.iam.presentation.rest;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.agroleak.iam.application.*;
import pe.edu.upc.agroleak.iam.presentation.rest.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/iam/users")
@Tag(name = "IAM Users")
public class UserController {
    private final CurrentUser current;
    private final UserQueryService users;

    public UserController(CurrentUser current, UserQueryService users) {
        this.current = current;
        this.users = users;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(users.getById(current.id()));
    }
}
