package com.smartwaste.mqtt;

import com.smartwaste.dto.BinUpdateRequest;
import com.smartwaste.service.BinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

/**
 * MQTT Message Handler
 *
 * Subscribes to topic: "bins/data"
 * Expected payload format: "binId,fillLevel"
 * Example:  "BIN-001,72.5"
 *
 * On receipt:
 *   1. Parses the payload
 *   2. Updates the bin in the database
 *   3. Recalculates status (EMPTY / HALF / FULL)
 *   4. Triggers alert if fill level > 80%
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MqttMessageHandler implements MessageHandler {

    private final BinService binService;

    @Override
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) throws MessagingException {
        String payload = message.getPayload().toString().trim();
        log.info("MQTT message received: [{}]", payload);

        try {
            MqttPayload parsed = parsePayload(payload);
            BinUpdateRequest request = new BinUpdateRequest();
            request.setBinCode(parsed.binCode());
            request.setFillLevel(parsed.fillLevel());

            binService.updateBin(request);
            log.info("Bin [{}] updated via MQTT. FillLevel={}%", parsed.binCode(), parsed.fillLevel());

        } catch (InvalidMqttPayloadException ex) {
            log.warn("Invalid MQTT payload: '{}'. Reason: {}", payload, ex.getMessage());
        } catch (Exception ex) {
            log.error("Error processing MQTT message: '{}'. Error: {}", payload, ex.getMessage(), ex);
        }
    }

    /**
     * Parses the MQTT payload.
     *
     * Supported formats:
     *   "BIN-001,72.5"          → simple format
     *   "BIN-001,72.5,12.97,77.59"  → with lat/lon
     */
    private MqttPayload parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new InvalidMqttPayloadException("Payload is empty");
        }

        String[] parts = payload.split(",");
        if (parts.length < 2) {
            throw new InvalidMqttPayloadException(
                    "Expected format 'binCode,fillLevel' but got: " + payload);
        }

        String binCode = parts[0].trim();
        if (binCode.isEmpty()) {
            throw new InvalidMqttPayloadException("Bin code is blank");
        }

        double fillLevel;
        try {
            fillLevel = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException ex) {
            throw new InvalidMqttPayloadException("Invalid fillLevel value: " + parts[1]);
        }

        if (fillLevel < 0 || fillLevel > 100) {
            throw new InvalidMqttPayloadException(
                    "fillLevel must be between 0 and 100, got: " + fillLevel);
        }

        return new MqttPayload(binCode, fillLevel);
    }

    private record MqttPayload(String binCode, double fillLevel) {}

    public static class InvalidMqttPayloadException extends RuntimeException {
        public InvalidMqttPayloadException(String message) {
            super(message);
        }
    }
}
