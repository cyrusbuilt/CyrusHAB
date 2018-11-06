package net.cyrusbuilt.cyrushab.core.telemetry;

import net.cyrusbuilt.cyrushab.core.things.Packet;
import net.cyrusbuilt.cyrushab.core.things.ThingParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Represents a system status packet for transmission over MQTT.
 */
public class SystemStatusPacket implements Packet {
    private SystemStatus _status = SystemStatus.UNKNOWN;
    private String _clientID = StringUtils.EMPTY;
    private Timestamp _timestamp = null;

    /**
     * Default private constructor only used by {@link Builder}.
     */
    private SystemStatusPacket() { }

    /**
     * Constructs a new instance of {@link SystemStatusPacket} with the client ID, status, and timestamp.
     * @param clientID The ID of the client.
     * @param status The system status.
     * @param timestamp The timestamp.
     */
    public SystemStatusPacket(String clientID, SystemStatus status, Timestamp timestamp) {
        _clientID = clientID;
        _status = status;
        _timestamp = timestamp;
    }

    /**
     * (non-Javdoc)
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
     * Gets the system status.
     * @return The status.
     */
    public SystemStatus getStatus() {
        return _status;
    }

    /**
     * Sets the system status. Only used by {@link Builder}.
     * @param status The system status.
     */
    private void setStatus(SystemStatus status) {
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
     * generated. If the timestamp was not specified, then the current local date/time will be used. If status was not
     * specified, then {@link SystemStatus#UNKNOWN} will be used.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "client_id": "some_client",
     *     "status": 0,
     *     "timestamp": "2018-10-17 10:36:43"
     * }
     */
    @Override
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        int status = _status.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HABSystem.SYS_CLIENT_ID, clientID);
        jsonObject.put(HABSystem.SYS_STATUS, status);
        jsonObject.put(HABSystem.SYS_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link SystemStatusPacket} objects. Allows easier control over all the flags, as well as help
     * constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<SystemStatusPacket> {
        private SystemStatusPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new SystemStatusPacket();
        }

        /**
         * Sets the system's client ID. If null, an empty string, or not set, then a client ID will be generated.
         * @param clientID The client ID.
         */
        public Builder setClientID(String clientID) {
            _packet.setClientID(clientID);
            return this;
        }

        /**
         * Sets the system status. If not set, then {@link SystemStatus#UNKNOWN} will be used.
         * @param status The system status.
         */
        public Builder setStatus(SystemStatus status) {
            _packet.setStatus(status);
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
         * Combine all of the options that have been set and return a new {@link SystemStatusPacket}.
         */
        @Override
        public SystemStatusPacket build() {
            if (StringUtils.isBlank(_packet.getClientID())) {
                _packet.setClientID(MqttClient.generateClientId());
            }

            if (_packet.getTimestamp() == null) {
                _packet.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            }

            return _packet;
        }
    }

    /**
     * Parses a {@link SystemStatusPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link SystemStatusPacket} populated with
     *      * the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static SystemStatusPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            String clientID = (String)jsonObject.get(HABSystem.SYS_CLIENT_ID);
            SystemStatus status = SystemStatus.NORMAL.getType((int)(long)jsonObject.get(HABSystem.SYS_STATUS));
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(HABSystem.SYS_TIMESTAMP));

            return new SystemStatusPacket.Builder()
                    .setClientID(clientID)
                    .setStatus(status)
                    .setTimestamp(tstamp)
                    .build();
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
