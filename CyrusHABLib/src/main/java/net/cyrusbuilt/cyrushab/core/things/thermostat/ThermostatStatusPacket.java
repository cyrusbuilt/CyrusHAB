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
 * Represents a thermostat status packet for transmission over MQTT.
 */
public class ThermostatStatusPacket {
    private int _id = -1;
    private String _name = StringUtils.EMPTY;
    private String _clientID = StringUtils.EMPTY;
    private ThermostatState _state = ThermostatState.UNKNOWN;
    private ThermostatMode _mode = ThermostatMode.OFF;
    private boolean _isEnabled = true;
    private boolean _isReadonly = false;
    private Timestamp _timestamp = null;

    /**
     * Default constructor.
     */
    public ThermostatStatusPacket() {}

    /**
     * Creates a new instance of {@link ThermostatStatusPacket} packet the id, name, and client ID.
     * @param id The Thing ID.
     * @param name The Thing name.
     * @param clientID The client ID. This should be the same as the system's client ID.
     */
    public ThermostatStatusPacket(int id, String name, String clientID) {
        _id = id;
        _name = name;
        _clientID = clientID;
    }

    /**
     * Gets the Thing ID.
     * @return The ID.
     */
    public int getID() {
        return _id;
    }

    /**
     * Sets the Thing ID.
     * @param id The ID.
     */
    public void setID(int id) {
        _id = id;
    }

    /**
     * Gets the name of the Switch.
     * @return The name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the name of the Switch.
     * @param name The name.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Gets the client ID.
     * @return The client ID.
     */
    public String getClientID() {
        return _clientID;
    }

    /**
     * Sets the client ID. This should be the same as the system's client ID.
     * @param clientID The client ID.
     */
    public void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * Gets the current state.
     * @return The current state.
     */
    public ThermostatState getState() {
        return _state;
    }

    /**
     * Sets the current state.
     * @param state The current state.
     */
    public void setState(ThermostatState state) {
        _state = state;
    }

    /**
     * Gets the current operating mode.
     * @return The current mode.
     */
    public ThermostatMode getMode() {
        return _mode;
    }

    /**
     * Sets the operating mode.
     * @param mode The mode.
     */
    public void setMode(ThermostatMode mode) {
        _mode = mode;
    }

    /**
     * Gets whether or not the Switch is enabled.
     * @return true if enabled; Otherwise, false.
     */
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Sets whether or not the Switch is enabled.
     * @param enabled Set true to enable.
     */
    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     * Gets whether or not the Switch is read-only.
     * @return true if read-only; Otherwise, false.
     */
    public boolean isReadonly() {
        return _isReadonly;
    }

    /**
     * Sets whether the Switch is read-only.
     * @param readonly Set true if read-only.
     */
    public void setReadonly(boolean readonly) {
        _isReadonly = readonly;
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
    public void setTimestamp(Timestamp timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Builds a JSON string representation of the packet data. If client ID was not specified, then one will be randomly
     * generated. If the timestamp was not specified, then the current local date/time will be used. If a name was not
     * specified, then "(None)" will be used. All other values will be default unless set otherwise.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "client_id": "some_id",
     *     "id": 1,
     *     "name": "Main thermostat",
     *     "state": 2,
     *     "mode": 2,
     *     "type": 1,
     *     "enabled": true,
     *     "readonly": false,
     *     "timestamp": "2018-10-17 15:14:51"
     * }
     */
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        String name = _name;
        if (StringUtils.isBlank(name)) {
            name = "(None)";
        }

        int state = _state.getValue();
        int type = ThingType.SWITCH.getValue();
        int mode = _mode.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Thing.THING_NAME, name);
        jsonObject.put(Thing.THING_ID, _id);
        jsonObject.put(Thing.THING_STATE, state);
        jsonObject.put(Thermostat.THERMOSTAT_MODE, mode);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, _isReadonly);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link ThermostatStatusPacket} objects. Allows easier control over all the flags, as well as
     * help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder {
        private ThermostatStatusPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new ThermostatStatusPacket();
        }

        /**
         * Sets the client ID. Should be the same as the system's client ID.
         * @param clientID The client ID.
         */
        public Builder setClientID(String clientID) {
            _packet.setClientID(clientID);
            return this;
        }

        /**
         * Sets the Thing ID.
         * @param id The ID.
         */
        public Builder setID(int id) {
            _packet.setID(id);
            return this;
        }

        /**
         * Sets the thermostat name.
         * @param name The name of the thermostat.
         */
        public Builder setName(String name) {
            _packet.setName(name);
            return this;
        }

        /**
         * Sets the state of the thermostat.
         * @param state The state.
         */
        public Builder setState(ThermostatState state) {
            _packet.setState(state);
            return this;
        }

        /**
         * Sets the operating mode of the thermostat.
         * @param mode The operating mode.
         */
        public Builder setMode(ThermostatMode mode) {
            _packet.setMode(mode);
            return this;
        }

        /**
         * Sets whether or not the thermostat is enabled.
         * @param enabled Set true to enable.
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
         * Combine all of the options that have been set and return a new {@link ThermostatStatusPacket}.
         */
        public ThermostatStatusPacket build() {
            if (StringUtils.isBlank(_packet.getClientID())) {
                _packet.setClientID(MqttClient.generateClientId());
            }

            if (StringUtils.isBlank(_packet.getName())) {
                _packet.setName("(None)");
            }

            if (_packet.getTimestamp() == null) {
                _packet.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            }

            return _packet;
        }
    }

    /**
     * Parses a {@link ThermostatStatusPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link ThermostatStatusPacket} populated
     * with the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static ThermostatStatusPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.THERMOSTAT) {
                // This isn't a thermostat.
                throw new ThingParseException("The specified JSON is not for a Thermostat type.");
            }

            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String name = (String)jsonObject.get(Thing.THING_NAME);
            ThermostatState state = ThermostatState.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_STATE));
            ThermostatMode mode = ThermostatMode.OFF.getType((int)(long)jsonObject.get(Thermostat.THERMOSTAT_MODE));
            boolean isEnabled = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean isReadonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new Builder()
                    .setClientID(clientID)
                    .setName(name)
                    .setID(id)
                    .setState(state)
                    .setMode(mode)
                    .setEnabled(isEnabled)
                    .setReadonly(isReadonly)
                    .setTimestamp(tstamp)
                    .build();
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
