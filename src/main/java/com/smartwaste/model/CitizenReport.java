package com.smartwaste.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imagePath;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false, length = 1000)
    private String description;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String reporterContact;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
    }
}
