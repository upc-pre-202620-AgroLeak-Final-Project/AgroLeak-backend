package pe.edu.upc.agroleak.farm.domain.model;

import jakarta.persistence.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "fields", indexes = @Index(name = "idx_field_farmId", columnList = "farm_id"))
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, updatable = false)
    private UUID farmId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal areaHectares;
    @Column(length = 1000)
    private String description;

    protected Field() {
    }

    public Field(UUID farmId, String name, BigDecimal areaHectares, String description) {
        this.farmId = farmId;
        this.name = name;
        this.areaHectares = areaHectares;
        this.description = description;
    }

    public void update(String name, BigDecimal areaHectares, String description) {
        this.name = name;
        this.areaHectares = areaHectares;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFarmId() {
        return farmId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAreaHectares() {
        return areaHectares;
    }

    public String getDescription() {
        return description;
    }
}
