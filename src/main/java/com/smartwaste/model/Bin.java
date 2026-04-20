package com.smartwaste.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String binCode;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double fillLevel;  // 0.0 - 100.0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BinStatus status;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedTime;

    @PrePersist
    protected void onCreate() {
        this.lastUpdatedTime = LocalDateTime.now();
        recalculateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedTime = LocalDateTime.now();
        recalculateStatus();
    }

    public void recalculateStatus() {
        if (this.fillLevel == null) return;
        if (this.fillLevel < 40.0) {
            this.status = BinStatus.EMPTY;
        } else if (this.fillLevel <= 80.0) {
            this.status = BinStatus.HALF;
        } else {
            this.status = BinStatus.FULL;
        }
    }
}
