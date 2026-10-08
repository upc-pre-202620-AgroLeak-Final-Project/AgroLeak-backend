package pe.edu.upc.agroleak.iam.presentation.rest.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(@NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName,
                              @NotBlank @Email @Size(max = 254) String email,
                              @NotBlank @Size(min = 8, max = 72) String password) {
}
