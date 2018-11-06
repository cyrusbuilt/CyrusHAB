package net.cyrusbuilt.cyrushab.core.application;

import net.cyrusbuilt.cyrushab.core.telemetry.HABSystem;
import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatus;
import net.cyrusbuilt.cyrushab.core.things.Packet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Represents a system heartbeat packet for transmission over MQTT. This is is used to announce that the system is
 * online and operational.
 */
public class HeartBeatPacket implements Packet {
    private String _clientID;
    private String _hostID;
    private SystemStatus _status = SystemStatus.UNKNOWN;
    private Timestamp _timestamp;

    /**
     * Constructs a new instance of {@link HeartBeatPacket}.
     */
    public HeartBeatPacket() {}

    /**
     * (non-Javadoc)
     * @see Packet#getClientID()
     */
    @Override
    public String getClientID() {
        return _clientID;
    }

    /**
     * (non-Javadoc)
     * @see Packet#setClientID(String)
     */
    @Override
    public void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * Gets the host ID (the host system's client ID)
     * @return The host ID.
     */
    public String getHostID() {
        return _hostID;
    }

    /**
     * Sets the host ID (the host system's client ID).
     * @param hostID The host ID.
     */
    public void setHostID(String hostID) {
        _hostID = hostID;
    }

    /**
     * Gets the system's state.
     * @return The system status.
     */
    public SystemStatus getStatus() {
        return _status;
    }

    /**
     * Sets the system's state.
     * @param status The system status.
     */
    public void setSystemStatus(SystemStatus status) {
        _status = status;
    }

    /**
     * (non-Javadoc)
     * @see Packet#getTimestamp()
     */
    @Override
    public Timestamp getTimestamp() {
        return _timestamp;
    }

    /**
     * (non-Javadoc)
     * @see Packet#setTimestamp(Timestamp)
     */
    @Override
    public void setTimestamp(Timestamp timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Builds a JSON string representation of the packet data. If client ID was not specified, then one will be randomly
     * generated. If the timestamp was not specified, then the current local date/time will be used. If the host ID was
     * not specified, the default will be used.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "client_id": "hab_app_1",
     *     "host_id": "hab_system_1",
     *     "status": 0,
     *     "timestamp": "2018-10-31 11:17:23.47"
     * }
     */
    @Override
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        String hostID = _hostID;
        if (StringUtils.isBlank(hostID)) {
            hostID = HABApp.APP_DEFAULT_HOST_ID;
        }

        int status = _status.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HABApp.APP_CLIENT_ID, clientID);
        jsonObject.put(HABApp.APP_HOST_ID, hostID);
        jsonObject.put(HABApp.APP_TIMESTAMP, tstamp.toString());
        jsonObject.put(HABSystem.SYS_STATUS, status);
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link HeartBeatPacket} objects. Allows easier control over all the flags, as well as help
     * constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<HeartBeatPacket> {
        private HeartBeatPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new HeartBeatPacket();
        }

        /**
         * Sets the client ID.
         * @param clientID The client ID.
         */
        public Builder setClientID(String clientID) {
            _packet.setClientID(clientID);
            return this;
        }

        /**
         * Sets the host ID (the host system's client ID).
         * @param hostID The host ID.
         */
        public Builder setHostID(String hostID) {
            _packet.setHostID(hostID);
            return this;
        }

        /**
         * (non-Javadoc)
         * @see Packet.Builder#setTimestamp(Timestamp)
         */
        @Override
        public Builder setTimestamp(Timestamp timestamp) {
            _packet.setTimestamp(timestamp);
            return this;
        }

        /**
         * Sets the system status.
         * @param status The status.
         */
        public Builder setStatus(SystemStatus status) {
            _packet.setSystemStatus(status);
            return this;
        }

        /**
         * Combine all of the options that have been set and return a new {@link HeartBeatPacket}.
         */
        @Override
        public HeartBeatPacket build() {
            if (StringUtils.isBlank(_packet.getClientID())) {
                _packet.setClientID(MqttClient.generateClientId());
            }

            if (StringUtils.isBlank(_packet.getHostID())) {
                _packet.setHostID(HABApp.APP_DEFAULT_HOST_ID);
            }

            if (_packet.getTimestamp() == null) {
                _packet.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            }

            return _packet;
        }
    }
}
