package net.cyrusbuilt.cyrushab.core.telemetry;

import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Represents a system control packet for transmission over MQTT.
 */
public class SystemControlPacket {
    private SystemCommand _command = SystemCommand.UNKNOWN;
    private String _clientID = StringUtils.EMPTY;
    private Timestamp _timestamp = null;

    /**
     * Constructs a new instance of {@link SystemControlPacket}.
     */
    private SystemControlPacket() {}

    /**
     * Constructs a new instance of {@link SystemControlPacket} with the command, client ID, and timestamp.
     * @param command The control command.
     * @param clientID The client ID.
     * @param timestamp The timestamp.
     */
    public SystemControlPacket(SystemCommand command, String clientID, Timestamp timestamp) {
        _command = command;
        _clientID = clientID;
        _timestamp = timestamp;
    }

    /**
     * Gets the control command.
     * @return The command.
     */
    public SystemCommand getCommand() {
        return _command;
    }

    /**
     * Sets the control command.
     * @param command The command.
     */
    private void setCommand(SystemCommand command) {
        _command = command;
    }

    /**
     * Gets the client ID.
     * @return The client ID.
     */
    public String getClientID() {
        return _clientID;
    }

    /**
     * Sets the client ID.
     * @param clientID The client ID.
     */
    private void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * Gets the timestamp.
     * @return The timestamp.
     */
    public Timestamp getTimestamp() {
        return _timestamp;
    }

    /**
     * Sets the timestamp.
     * @param timestamp The timestamp.
     */
    private void setTimestamp(Timestamp timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Builds a JSON string representation of the packet data. If client ID was not specified, then one will be randomly
     * generated. If the timestamp was not specified, then the current local date/time will be used. If a command was
     * not specified, then {@link SystemCommand#UNKNOWN} will be used.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "client_id": "hab_daemon_1",
     *     "command": 0,
     *     "timestamp": "2018-10-24 15:34:42.31"
     * }
     */
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        int command = _command.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HABSystem.SYS_CLIENT_ID, clientID);
        jsonObject.put(HABSystem.SYS_COMMAND, command);
        jsonObject.put(HABSystem.SYS_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link SystemControlPacket} objects. Allows easier control over all the flags, as well as help
     * constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder {
        private SystemControlPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new SystemControlPacket();
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
         * Sets the control command.
         * @param command The command.
         */
        public Builder setCommand(SystemCommand command) {
            _packet.setCommand(command);
            return this;
        }

        /**
         * Sets the timestamp.
         * @param timestamp The timestamp.
         */
        public Builder setTimestamp(Timestamp timestamp) {
            _packet.setTimestamp(timestamp);
            return this;
        }

        /**
         * Combine all of the options that have been set and return a new {@link SystemControlPacket}.
         */
        public SystemControlPacket build() {
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
     * Parses a {@link SystemControlPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link SystemControlPacket} populated
     * with the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static SystemControlPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.SYSTEM) {
                // This is not a system control packet.
                throw new ThingParseException("The specified JSON is not for a System type.");
            }

            String clientID = (String)jsonObject.get(HABSystem.SYS_CLIENT_ID);
            SystemCommand command = SystemCommand.UNKNOWN.getType((int)(long)jsonObject.get(HABSystem.SYS_COMMAND));
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(HABSystem.SYS_TIMESTAMP));

            return new SystemControlPacket.Builder()
                    .setClientID(clientID)
                    .setCommand(command)
                    .setTimestamp(tstamp)
                    .build();
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
