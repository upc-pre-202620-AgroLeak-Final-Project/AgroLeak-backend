package pe.edu.upc.agroleak.iam.presentation.rest.dto;

import java.util.UUID;

import pe.edu.upc.agroleak.iam.domain.model.*;

public record UserResponse(UUID id, String firstName, String lastName, String email, Role role) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getRole());
    }
}
