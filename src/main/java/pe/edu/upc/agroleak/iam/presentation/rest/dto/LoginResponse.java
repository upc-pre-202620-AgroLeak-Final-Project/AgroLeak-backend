package pe.edu.upc.agroleak.iam.presentation.rest.dto;

public record LoginResponse(String accessToken, String tokenType, UserResponse user) {
}
