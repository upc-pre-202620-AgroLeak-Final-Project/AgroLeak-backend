package pe.edu.upc.agroleak.farm.domain.model;

import jakarta.persistence.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "crops", indexes = @Index(name = "idx_crop_sectorId", columnList = "sector_id"))
public class Crop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, updatable = false)
    private UUID sectorId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 100)
    private String variety;
    @Column(nullable = false)
    private LocalDate plantedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CropStatus status;

    protected Crop() {
    }

    public Crop(UUID sectorId, String name, String variety, LocalDate plantedAt, CropStatus status) {
        this.sectorId = sectorId;
        this.name = name;
        this.variety = variety;
        this.plantedAt = plantedAt;
        this.status = status;
    }

    public void update(String name, String variety, LocalDate plantedAt, CropStatus status) {
        this.name = name;
        this.variety = variety;
        this.plantedAt = plantedAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSectorId() {
        return sectorId;
    }

    public String getName() {
        return name;
    }

    public String getVariety() {
        return variety;
    }

    public LocalDate getPlantedAt() {
        return plantedAt;
    }

    public CropStatus getStatus() {
        return status;
    }
}
