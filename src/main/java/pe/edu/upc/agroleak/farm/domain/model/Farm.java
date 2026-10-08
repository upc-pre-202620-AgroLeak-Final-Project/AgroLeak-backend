package pe.edu.upc.agroleak.farm.domain.model;

import jakarta.persistence.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "farms", indexes = @Index(name = "idx_farm_ownerId", columnList = "owner_id"))
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 255)
    private String location;
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal areaHectares;
    @Column(nullable = false, updatable = false)
    private UUID ownerId;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected Farm() {
    }

    public Farm(String name, String location, BigDecimal areaHectares, UUID ownerId) {
        this.name = name;
        this.location = location;
        this.areaHectares = areaHectares;
        this.ownerId = ownerId;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update(String name, String location, BigDecimal areaHectares) {
        this.name = name;
        this.location = location;
        this.areaHectares = areaHectares;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getAreaHectares() {
        return areaHectares;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
