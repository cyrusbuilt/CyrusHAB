package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.things.Packet;
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
 * Represents a switch control packet for transmission over MQTT.
 */
public class SwitchControlPacket implements Packet {
    private int _id = -1;
    private String _clientID = StringUtils.EMPTY;
    private SwitchState _state = SwitchState.OFF;
    private boolean _isEnabled = false;
    private boolean _isReadonly = false;
    private Timestamp _timestamp;

    /**
     * Constructs a new instance of
     */
    public SwitchControlPacket() {}

    /**
     * Gets the Thing ID of the switch.
     * @return The ID.
     */
    public int getID() {
        return _id;
    }

    /**
     * Sets the Thing ID of the switch.
     * @param id The ID.
     */
    public void setID(int id) {
        _id = id;
    }

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
     * Gets the switch state.
     * @return The state.
     */
    public SwitchState getState() {
        return _state;
    }

    /**
     * Sets the switch state.
     * @param state The state.
     */
    public void setState(SwitchState state) {
        _state = state;
    }

    /**
     * Gets whether or not the switch is enabled.
     * @return true if enabled; Otherwise, false.
     */
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Sets whether or not the switch is enabled.
     * @param enabled Set true to enable.
     */
    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     * Gets whether or not the switch is read-only.
     * @return true if the switch is read-only.
     */
    public boolean isReadonly() {
        return _isReadonly;
    }

    /**
     * Sets whether or not the switch is read-only.
     * @param readonly Set true if read-only.
     */
    public void setReadonly(boolean readonly) {
        _isReadonly = readonly;
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
     * generated. If the timestamp was not specified, then the current local date/time will be used. All other values
     * will be default unless set otherwise.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "id": 1,
     *     "client_id": "switch_1",
     *     "type": 2,
     *     "state": 0,
     *     "readonly": false,
     *     "enabled": true,
     *     "timestamp": "2018-10-28 13:23:31.41"
     * }
     */
    @Override
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        int state = _state.getValue();
        int type = ThingType.SWITCH.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_ID, _id);
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_STATE, state);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, _isReadonly);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * Builder class for {@link SwitchControlPacket} objects. Allows easier control over all the flags, as well as
     * help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<SwitchControlPacket> {
        private SwitchControlPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new SwitchControlPacket();
        }

        /**
         * Sets the Thing ID of the switch.
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
         * Sets the switch state.
         * @param state The state.
         */
        public Builder setState(SwitchState state) {
            _packet.setState(state);
            return this;
        }

        /**
         * Sets whether or not the switch is enabled.
         * @param enabled Set true if enabled.
         */
        public Builder setEnabled(boolean enabled) {
            _packet.setEnabled(enabled);
            return this;
        }

        /**
         * Sets whether or not the switch readonly.
         * @param readonly Set true if readonly.
         */
        public Builder setReadonly(boolean readonly) {
            _packet.setReadonly(readonly);
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
         * Combine all of the options that have been set and return a new {@link SwitchControlPacket}.
         */
        @Override
        public SwitchControlPacket build() {
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
     * Parses a {@link SwitchControlPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link SwitchControlPacket} populated
     * with the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static SwitchControlPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.SWITCH) {
                // This is not a switch control packet.
                throw new ThingParseException("The specified JSON is not for a Switch type.");
            }

            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            SwitchState state = SwitchState.OFF.getType((int)(long)jsonObject.get(Thing.THING_STATE));
            boolean enable = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean readonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new Builder()
                    .setThingID(id)
                    .setClientID(clientID)
                    .setState(state)
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
