package net.cyrusbuilt.cyrushab.core.things.door;

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
 * Represents a Door status packet for transmission over MQTT.
 */
public class DoorStatusPacket implements Packet {
    private int _thingID = -1;
    private String _clientID = StringUtils.EMPTY;
    private DoorState _state = DoorState.UNKNOWN;
    private boolean _isEnabled = false;
    private boolean _isReadonly = false;
    private boolean _isLocked = false;
    private Timestamp _timestamp;

    /**
     * Constructs a new instance of {@link DoorStatusPacket}.
     */
    public DoorStatusPacket() {}

    /**
     * Gets the Thing ID.
     * @return The ID.
     */
    public int getThingID() {
        return _thingID;
    }

    /**
     * Sets the Thing ID.
     * @param id The ID.
     */
    public void setThingID(int id) {
        _thingID = id;
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
     * Gets the door state.
     * @return The door state.
     */
    public DoorState getState() {
        return _state;
    }

    /**
     * Sets the door state.
     * @param state The door state.
     */
    public void setState(DoorState state) {
        _state = state;
    }

    /**
     * Gets whether the door is enabled.
     * @return true if enabled; Otherwise, false.
     */
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Sets whether the door is enabled.
     * @param enabled Set true to enable.
     */
    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     * Gets whether the door is read-only.
     * @return true if read-only; Otherwise, false.
     */
    public boolean isReadonly() {
        return _isReadonly;
    }

    /**
     * Sets whether the door is read-only.
     * @param readonly Set true if read-only.
     */
    public void setReadonly(boolean readonly) {
        _isReadonly = readonly;
    }

    /**
     * Gets whether the door is locked.
     * @return true if locked; Otherwise, false.
     */
    public boolean isLocked() {
        return _isLocked;
    }

    /**
     * Sets whether the door is locked.
     * @param locked Set true if locked.
     */
    public void setLocked(boolean locked) {
        _isLocked = locked;
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
     *     "client_id": "door_1",
     *     "state": 0,
     *     "lock_enabled": false,
     *     "type": 5,
     *     "enabled": true,
     *     "readonly": false,
     *     "timestamp": "2018-11-01 15:43:26.31"
     * }
     */
    @Override
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        int state = _state.getValue();
        int type = ThingType.DOOR.getValue();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_ID, _thingID);
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Thing.THING_STATE, state);
        jsonObject.put(Door.DOOR_LOCK_ENABLED, _isLocked);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, _isReadonly);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * A builder class for {@link DoorStatusPacket} objects. Allows easier control over all the flags, as well as help
     * constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<DoorStatusPacket> {
        private DoorStatusPacket _packet;

        /**
         * Constructs a new instance of {@link DoorStatusPacket}.
         */
        public Builder() {
            _packet = new DoorStatusPacket();
        }

        /**
         * Sets the Thing ID.
         * @param id The ID.
         */
        public Builder setThingID(int id) {
            _packet.setThingID(id);
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
         * Sets the door state.
         * @param state The door state.
         */
        public Builder setState(DoorState state) {
            _packet.setState(state);
            return this;
        }

        /**
         * Sets whether the door is locked.
         * @param locked Set true if locked.
         */
        public Builder setLocked(boolean locked) {
            _packet.setLocked(locked);
            return this;
        }

        /**
         * Sets whether the door is enabled.
         * @param enabled Set true if enabled.
         */
        public Builder setEnabled(boolean enabled) {
            _packet.setEnabled(enabled);
            return this;
        }

        /**
         * Sets whether the door is read-only.
         * @param readonly Set true if read-only.
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
         * Combine all of the options that have been set and return a new {@link DoorStatusPacket}.
         */
        @Override
        public DoorStatusPacket build() {
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
     * Parses a {@link DoorStatusPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link DoorStatusPacket} populated with
     * the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static DoorStatusPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.DOOR) {
                // This is not a Door.
                throw new ThingParseException("The specified JSON is not for a Door type.");
            }

            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            DoorState state = DoorState.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_STATE));
            boolean isEnabled = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean isReadonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            boolean isLocked = (boolean)jsonObject.get(Door.DOOR_LOCK_ENABLED);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new DoorStatusPacket.Builder()
                    .setThingID(id)
                    .setClientID(clientID)
                    .setState(state)
                    .setLocked(isLocked)
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
