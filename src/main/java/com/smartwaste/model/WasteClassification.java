package com.smartwaste.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "waste_classifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imagePath;

    @Column(nullable = false)
    private String wasteType;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private LocalDateTime classifiedAt;

    private String additionalInfo;

    @PrePersist
    protected void onCreate() {
        this.classifiedAt = LocalDateTime.now();
    }
}
