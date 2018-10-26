package net.cyrusbuilt.cyrushab.core.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * The exception that is thrown by the framework when an MQTT error occurs.
 */
public class HABMqttException extends Exception {
    /**
     * Constructs a new instance of {@link HABMqttException} with the {@link MqttException} that is the cause of the
     * exception.
     * @param ex The cause of the exception.
     */
    public HABMqttException(MqttException ex) {
        super(ex);
    }
}
