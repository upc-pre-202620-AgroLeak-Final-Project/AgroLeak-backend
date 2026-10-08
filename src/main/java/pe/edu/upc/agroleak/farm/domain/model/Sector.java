package pe.edu.upc.agroleak.farm.domain.model;

import jakarta.persistence.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "sectors", indexes = @Index(name = "idx_sector_fieldId", columnList = "field_id"))
public class Sector {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, updatable = false)
    private UUID fieldId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal areaHectares;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SectorStatus status;

    protected Sector() {
    }

    public Sector(UUID fieldId, String name, BigDecimal areaHectares, SectorStatus status) {
        this.fieldId = fieldId;
        this.name = name;
        this.areaHectares = areaHectares;
        this.status = status;
    }

    public void update(String name, BigDecimal areaHectares, SectorStatus status) {
        this.name = name;
        this.areaHectares = areaHectares;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFieldId() {
        return fieldId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAreaHectares() {
        return areaHectares;
    }

    public SectorStatus getStatus() {
        return status;
    }
}
