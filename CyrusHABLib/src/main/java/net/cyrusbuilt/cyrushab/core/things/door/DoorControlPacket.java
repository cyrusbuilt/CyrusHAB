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
 * Represents a Door control packet for transmission over MQTT.
 */
public class DoorControlPacket implements Packet {
    private int _id = -1;
    private String _clientID = StringUtils.EMPTY;
    private DoorCommand _command = DoorCommand.UNKNOWN;
    private boolean _enabled = false;
    private boolean _readonly = false;
    private boolean _lockEnabled = false;
    private Timestamp _timestamp;

    /**
     * Construct a new instance of {@link DoorControlPacket}.
     */
    public DoorControlPacket() {}

    /**
     * Gets the Thing ID.
     * @return The ID.
     */
    public int getThingID() {
        return _id;
    }

    /**
     * Sets the Thing ID.
     * @param id The ID.
     */
    public void setThingID(int id) {
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
     * Gets the door command.
     * @return The door command.
     */
    public DoorCommand getCommand() {
        return _command;
    }

    /**
     * Sets the door command.
     * @param command The door command.
     */
    public void setCommand(DoorCommand command) {
        _command = command;
    }

    /**
     * Gets whether the door lock is enabled.
     * @return true if the door lock is enabled; Otherwise, false.
     */
    public boolean isLockEnabled() {
        return _lockEnabled;
    }

    /**
     * Enable the door lock.
     * @param enable Set true if the lock is enabled.
     */
    public void enableLock(boolean enable) {
        _lockEnabled = enable;
    }

    /**
     * Gets whether the light is enabled.
     * @return true if enabled; Otherwise, false.
     */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * Sets whether the light is enabled.
     * @param enabled Set true to enable.
     */
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /**
     * Gets the type of Thing (always DOOR).
     * @return The Thing type.
     */
    public ThingType getType() {
        return ThingType.DOOR;
    }

    /**
     * Gets whether the light is read-only.
     * @return true if read-only; Otherwise, false.
     */
    public boolean isReadonly() {
        return _readonly;
    }

    /**
     * Sets whether the light is read-only.
     * @param readonly Set true if read-only.
     */
    public void setReadonly(boolean readonly) {
        _readonly = readonly;
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
     *     "id": 5,
     *     "client_id": "door_1",
     *     "command": 1,
     *     "lock_enabled": false,
     *     "type": 5,
     *     "readonly", false,
     *     "enabled": true,
     *     "timestamp": "2018-11-02 14:53.27.18"
     * }
     */
    @Override
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        int type = ThingType.DOOR.getValue();
        int command = _command.getValue();
        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_ID, _id);
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Door.DOOR_COMMAND, command);
        jsonObject.put(Door.DOOR_LOCK_ENABLED, _lockEnabled);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_READONLY, _readonly);
        jsonObject.put(Thing.THING_ENABLED, _enabled);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * A builder class for {@link DoorControlPacket} objects. Allows easier control over all the flags, as well
     * as help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<DoorControlPacket> {
        private DoorControlPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new DoorControlPacket();
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
         * Sets the door command.
         * @param command The door command.
         */
        public Builder setCommand(DoorCommand command) {
            _packet.setCommand(command);
            return this;
        }

        /**
         * Sets whether the door lock should be enabled.
         * @param enable Set true if enabled.
         */
        public Builder setLockEnabled(boolean enable) {
            _packet.enableLock(enable);
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
         * Sets whether the door is enabled.
         * @param enabled Set true if enabled.
         */
        public Builder setEnabled(boolean enabled) {
            _packet.setEnabled(enabled);
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
         * Combine all of the options that have been set and return a new {@link DoorControlPacket}.
         */
        @Override
        public DoorControlPacket build() {
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
     * Parses a {@link DoorControlPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link DoorControlPacket} populated with
     * the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static DoorControlPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.DOOR) {
                // This is not a door control packet.
                throw new ThingParseException("The specified JSON is not for a Door type.");
            }

            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            DoorCommand command = DoorCommand.UNKNOWN.getType((int)(long)jsonObject.get(Door.DOOR_COMMAND));
            boolean enable = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean readonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            boolean lockenable = (boolean)jsonObject.get(Door.DOOR_LOCK_ENABLED);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new DoorControlPacket.Builder()
                    .setThingID(id)
                    .setClientID(clientID)
                    .setCommand(command)
                    .setEnabled(enable)
                    .setReadonly(readonly)
                    .setLockEnabled(lockenable)
                    .setTimestamp(tstamp)
                    .build();
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
