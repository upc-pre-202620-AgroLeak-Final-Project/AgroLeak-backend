package pe.edu.upc.agroleak.iam.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank
    @Column(nullable = false, length = 254)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {}

    /**
     * passwordHash must already be encoded by the caller. This model does not
     * accept a raw-password workflow or perform password encoding.
     */
    public User(String firstName, String lastName, String email, String passwordHash, Role role) {
        this.firstName = required(firstName, "firstName", 100);
        this.lastName = required(lastName, "lastName", 100);
        this.email = required(email, "email", 254);
        this.passwordHash = required(passwordHash, "passwordHash", 255);
        if (role == null) throw new IllegalArgumentException("role es obligatorio");
        this.role = role;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " es obligatorio");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " excede la longitud máxima de " + maxLength);
        }
        return value;
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setActive(boolean active) { this.active = active; }
}
