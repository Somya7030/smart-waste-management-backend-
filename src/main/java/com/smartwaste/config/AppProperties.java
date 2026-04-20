package com.smartwaste.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "route")
@Data
public class AppProperties {
    private double weightFillLevel = 0.7;
    private double weightFullBonus = 50.0;
    private double clusterRadiusKm = 2.0;
}
