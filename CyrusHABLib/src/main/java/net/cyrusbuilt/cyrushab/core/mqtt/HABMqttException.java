package net.cyrusbuilt.cyrushab.core.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;

public class HABMqttException extends Exception {
    public HABMqttException(MqttException ex) {
        super(ex);
    }
}
