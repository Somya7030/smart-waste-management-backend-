package com.smartwaste.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.stereotype.Component;

/**
 * MQTT Publisher — used for testing and simulating IoT sensor data.
 * In production, real sensors publish directly to the broker.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MqttPublisher {

    private final MqttPahoClientFactory mqttClientFactory;

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String defaultTopic;

    /**
     * Publishes a message to the default bins/data topic.
     * Payload format: "binCode,fillLevel"
     */
    public void publishBinData(String binCode, double fillLevel) {
        String payload = binCode + "," + fillLevel;
        publish(defaultTopic, payload);
    }

    /**
     * Publishes a raw payload to a given topic.
     */
    public void publish(String topic, String payload) {
        String publishClientId = clientId + "-pub-" + System.currentTimeMillis();

        try {
            MqttClient client = new MqttClient(brokerUrl, publishClientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.connect(options);
            client.publish(topic, payload.getBytes(), 0, false);
            client.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}