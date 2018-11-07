package net.cyrusbuilt.cyrushab.core.things.motionsensor;

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
 * Represents a motion sensor status packet for transmission over MQTT.
 */
public class MotionSensorStatusPacket implements Packet {
    private int _thingID = -1;
    private String _clientID = StringUtils.EMPTY;
    private MotionSensorState _state = MotionSensorState.UNKNOWN;
    private boolean _isEnabled = false;
    private Timestamp _timestamp;

    /**
     * Constructs a new instance of {@link MotionSensorStatusPacket}.
     */
    public MotionSensorStatusPacket() {}

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
     * Gets whether the sensor is enabled.
     * @return true if enabled; Otherwise, false.
     */
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Sets whether the sensor is enabled.
     * @param enabled Set true if enabled.
     */
    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     * Gets the motion sensor state.
     * @return The current state.
     */
    public MotionSensorState getState() {
        return _state;
    }

    /**
     * Sets the motion sensor state.
     * @param state The current state.
     */
    public void setState(MotionSensorState state) {
        _state = state;
    }

    /**
     * Builds a JSON string representation of the packet data. If client ID was not specified, then one will be randomly
     * generated. If the timestamp was not specified, then the current local date/time will be used. All other values
     * will be default unless set otherwise.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "client_id": "motion_1",
     *     "id": 3,
     *     "state": 1,
     *     "type": 4,
     *     "enabled": true,
     *     "readonly": true,
     *     "timestamp": "2018-11-07 14:29:33.47"
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
        int type = ThingType.MOTION_SENSOR.getValue();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Thing.THING_CLIENT_ID, clientID);
        jsonObject.put(Thing.THING_STATE, state);
        jsonObject.put(Thing.THING_TYPE, type);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, true);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     * A builder class for {@link MotionSensorStatusPacket} objects. Allows easier control over all the flags, as well
     * as help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<MotionSensorStatusPacket> {
        private MotionSensorStatusPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new MotionSensorStatusPacket();
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
         * Sets the current state.
         * @param state The current state.
         */
        public Builder setState(MotionSensorState state) {
            _packet.setState(state);
            return this;
        }

        /**
         * Sets whether the sensor is enabled.
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
         * Combine all of the options that have been set and return a new {@link MotionSensorStatusPacket}.
         */
        @Override
        public MotionSensorStatusPacket build() {
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
     * Parses a {@link MotionSensorStatusPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link MotionSensorStatusPacket}
     * populated with the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
    @Nullable
    public static MotionSensorStatusPacket fromJsonString(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) obj;
            ThingType type = ThingType.UNKNOWN.getType((int) (long) jsonObject.get(Thing.THING_TYPE));
            if (type != ThingType.MOTION_SENSOR) {
                throw new ThingParseException("The specified JSON is not for a MotionSensor type.");
            }

            int id = (int)(long)jsonObject.get(Thing.THING_ID);
            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            MotionSensorState state = MotionSensorState.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_STATE));
            boolean isEnabled = (boolean)jsonObject.get(Thing.THING_ENABLED);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

            return new MotionSensorStatusPacket.Builder()
                    .setThingID(id)
                    .setClientID(clientID)
                    .setState(state)
                    .setEnabled(isEnabled)
                    .setTimestamp(tstamp)
                    .build();
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
