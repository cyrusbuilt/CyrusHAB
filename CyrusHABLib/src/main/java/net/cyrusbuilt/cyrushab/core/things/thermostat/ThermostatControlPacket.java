package net.cyrusbuilt.cyrushab.core.things.thermostat;

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
 * Represents a thermostat control packet for transmission over MQTT.
 */
public class ThermostatControlPacket {
    private int _id = -1;
    private String _clientID = StringUtils.EMPTY;
    private ThermostatState _state = ThermostatState.UNKNOWN;
    private ThermostatMode _mode = ThermostatMode.OFF;
    private boolean _isEnabled = false;
    private boolean _isReadonly = false;
    private Timestamp _timestamp;

    /**
     * Constructs a new instance of {@link ThermostatControlPacket}.
     */
    public ThermostatControlPacket() {}

    /**
     * Gets the Thing ID of the thermostat.
     * @return The ID.
     */
    public int getID() {
        return _id;
    }

    /**
     * Sets the Thing ID of the thermostat.
     * @param id The ID.
     */
    public void setID(int id) {
        _id = id;
    }

    /**
     * Gets the client ID of the thermostat.
     * @return The client ID.
     */
    public String getClientID() {
        return _clientID;
    }

    /**
     * Sets the client ID of the thermostat.
     * @param clientID The client ID.
     */
    public void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * Gets the thermostat mode.
     * @return The mode.
     */
    public ThermostatMode getMode() {
        return _mode;
    }

    /**
     * Sets the thermostat mode.
     * @param mode The mode.
     */
    public void setMode(ThermostatMode mode) {
        _mode = mode;
    }

    /**
     * Gets whether or not the thermostat is enabled.
     * @return true if enabled; Otherwise, false.
     */
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Enables/disables the thermostat.
     * @param enabled Set true to enable.
     */
    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     * Gets whether or not the thermostat is read-only.
     * @return true if read-only; Otherwise, false.
     */
    public boolean isReadonly() {
        return _isReadonly;
    }

    /**
     * Sets whether or not the device is read-only.
     * @param readonly Set true to make read-only.
     */
    public void setReadonly(boolean readonly) {
        _isReadonly = readonly;
    }

    /**
     * Gets the timestamp of the control packet.
     * @return The timestamp.
     */
    public Timestamp getTimestamp() {
        return _timestamp;
    }

    /**
     * Sets the timestamp of the control packet.
     * @param timestamp The timestamp.
     */
    public void setTimestamp(Timestamp timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Builds a JSON string representation of the packet data. If client ID was not specified, then one will be randomly
     * generated. If the timestamp was not specified, then the current local date/time will be used. If a name was not
     * specified, then "(None)" will be used. All other values will be default unless set otherwise.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "id": 1
     *     "client_id": "thermostat_1",
     *     "name": "Main thermostat",
     *     "state": 1,
     *     "mode": 1,
     *     "type": 1,
     *     "enabled": true,
     *     "readonly": false,
     *     "timestamp": "2018-10-28 12:56:23.41"
     * }
     */
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        int state = _state.getValue();
        int mode = _mode.getValue();
        int type = ThingType.THERMOSTAT.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_ID, _id);
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_STATE, state);
        jsonObject.put(Thermostat.THERMOSTAT_MODE, mode);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, _isReadonly);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link ThermostatControlPacket} objects. Allows easier control over all the flags, as well as
     * help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder {
        private ThermostatControlPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new ThermostatControlPacket();
        }

        /**
         * Sets the Thing ID.
         * @param id The ID.
         */
        public Builder setThingID(int id) {
            _packet.setID(id);
            return this;
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
         * Sets the mode.
         * @param mode The mode.
         */
        public Builder setMode(ThermostatMode mode) {
            _packet.setMode(mode);
            return this;
        }

        /**
         * Sets whether or not the thermostat is enabled.
         * @param enabled Set true if enabled.
         */
        public Builder setEnabled(boolean enabled) {
            _packet.setEnabled(enabled);
            return this;
        }

        /**
         * Sets whether or not the thermostat is read-only.
         * @param readonly Set true if read-only.
         */
        public Builder setReadonly(boolean readonly) {
            _packet.setReadonly(readonly);
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
         * Combine all of the options that have been set and return a new {@link ThermostatControlPacket}.
         */
        public ThermostatControlPacket build() {
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
     * Parses a {@link ThermostatControlPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link ThermostatControlPacket} populated
     * with the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static ThermostatControlPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.THERMOSTAT) {
                // This is not a thermostat control packet.
                throw new ThingParseException("The specified JSON is not for a Thermostat type.");
            }

            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            ThermostatMode mode = ThermostatMode.OFF.getType((int)(long)jsonObject.get(Thermostat.THERMOSTAT_MODE));
            boolean enable = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean readonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new Builder()
                    .setThingID(id)
                    .setClientID(clientID)
                    .setMode(mode)
                    .setEnabled(enable)
                    .setReadonly(readonly)
                    .setTimestamp(tstamp)
                    .build();
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
