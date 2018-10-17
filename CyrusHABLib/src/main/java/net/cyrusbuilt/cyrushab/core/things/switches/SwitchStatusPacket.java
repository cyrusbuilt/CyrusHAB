package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import net.cyrusbuilt.cyrushab.core.things.ThingParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Represents a Switch status packet for transmission over MQTT.
 */
public class SwitchStatusPacket {
    private int _id = -1;
    private String _name = StringUtils.EMPTY;
    private String _clientID = StringUtils.EMPTY;
    private SwitchState _state = SwitchState.OFF;
    private boolean _isEnabled = true;
    private boolean _isReadonly = false;
    private Timestamp _timestamp = null;

    /**
     * Default constructor.
     */
    public SwitchStatusPacket() { }

    /**
     * Constructs a new instance of {@link SwitchStatusPacket} with the ID, name, and client ID.
     * @param id The "Thing" (device) ID.
     * @param name The name of the Thing.
     * @param clientID The client ID. This should be the same as the system's client ID.
     */
    public SwitchStatusPacket(int id, String name, String clientID) {
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
     * Gets the Switch state. Default is {@link SwitchState#OFF}.
     * @return The state of the Switch.
     */
    public SwitchState getState() {
        return _state;
    }

    /**
     * Sets the state of the Switch.
     * @param state The state of the Switch.
     */
    public void setState(SwitchState state) {
        _state = state;
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
     *     "name": "Living room light",
     *     "state": 1,
     *     "type": 2,
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
            name = "(none)";
        }

        int state = _state.getValue();
        int type = ThingType.SWITCH.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Thing.THING_ID, _id);
        jsonObject.put(Thing.THING_NAME, name);
        jsonObject.put(Thing.THING_STATE, state);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, _isReadonly);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp);
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link SwitchStatusPacket} objects. Allows easier control over all the flags, as well as help
     * constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder {
        private SwitchStatusPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new SwitchStatusPacket();
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
         * Sets the Switch name.
         * @param name The name of the Switch.
         */
        public Builder setName(String name) {
            _packet.setName(name);
            return this;
        }

        /**
         * Sets the state of the Switch.
         * @param state The Switch state.
         */
        public Builder setState(SwitchState state) {
            _packet.setState(state);
            return this;
        }

        /**
         * Sets whether or not the Switch is enabled.
         * @param enabled Set true to enable.
         */
        public Builder setEnabled(boolean enabled) {
            _packet.setEnabled(enabled);
            return this;
        }

        /**
         * Sets whether or not the Switch is read-only.
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
         * Combine all of the options that have been set and return a new {@link SwitchStatusPacket}.
         */
        public SwitchStatusPacket build() {
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
     * Parses a {@link SwitchStatusPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link SwitchStatusPacket} populated with
     * the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    public static SwitchStatusPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.SWITCH) {
                // This isn't a switch.
                throw new ThingParseException("The specified JSON is not for a Switch type.");
            }

            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String name = (String)jsonObject.get(Thing.THING_NAME);
            SwitchState state = SwitchState.OFF.getType((int)(long)jsonObject.get(Thing.THING_STATE));
            boolean isEnabled = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean isReadonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new SwitchStatusPacket.Builder()
                    .setClientID(clientID)
                    .setID(id)
                    .setName(name)
                    .setState(state)
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
