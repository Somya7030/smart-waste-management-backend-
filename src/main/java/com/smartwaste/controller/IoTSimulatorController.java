package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.mqtt.MqttPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * IoT Simulator Controller
 * Allows triggering MQTT messages via REST for demo and testing purposes.
 * Remove or secure this in production.
 */
@RestController
@RequestMapping("/api/simulate")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class IoTSimulatorController {

    private final MqttPublisher mqttPublisher;

    /**
     * POST /api/simulate/bin
     * Simulates an IoT sensor publishing bin fill data via MQTT.
     *
     * Body: { "binCode": "BIN-001", "fillLevel": 85.0 }
     */
    @PostMapping("/bin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateBinUpdate(
            @RequestBody Map<String, Object> body) {
        String binCode  = (String) body.get("binCode");
        double fillLevel = Double.parseDouble(body.get("fillLevel").toString());

        log.info("Simulating IoT update for bin: {} fill: {}%", binCode, fillLevel);
        mqttPublisher.publishBinData(binCode, fillLevel);

        return ResponseEntity.ok(ApiResponse.success("IoT data simulated", Map.of(
                "binCode",   binCode,
                "fillLevel", fillLevel,
                "topic",     "bins/data",
                "payload",   binCode + "," + fillLevel
        )));
    }

    /**
     * POST /api/simulate/bulk
     * Simulates multiple bins at once.
     *
     * Body: [{ "binCode": "BIN-001", "fillLevel": 85.0 }, ...]
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateBulk(
            @RequestBody java.util.List<Map<String, Object>> updates) {
        int count = 0;
        for (Map<String, Object> update : updates) {
            String binCode   = (String) update.get("binCode");
            double fillLevel = Double.parseDouble(update.get("fillLevel").toString());
            mqttPublisher.publishBinData(binCode, fillLevel);
            count++;
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Bulk simulation complete", Map.of("published", count)));
    }
}
