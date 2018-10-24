package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.telemetry.HABSystem;
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
 *
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
     *
     */
    public ThermostatControlPacket() {}

    /**
     *
     * @return
     */
    public int getID() {
        return _id;
    }

    /**
     *
     * @param id
     */
    public void setID(int id) {
        _id = id;
    }

    /**
     *
     * @return
     */
    public String getClientID() {
        return _clientID;
    }

    /**
     *
     * @param clientID
     */
    public void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     *
     * @return
     */
    public ThermostatMode getMode() {
        return _mode;
    }

    /**
     *
     * @param mode
     */
    public void setMode(ThermostatMode mode) {
        _mode = mode;
    }

    /**
     *
     * @return
     */
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }

    /**
     *
     * @return
     */
    public boolean isReadonly() {
        return _isReadonly;
    }

    /**
     *
     * @param readonly
     */
    public void setReadonly(boolean readonly) {
        _isReadonly = readonly;
    }

    /**
     *
     * @return
     */
    public Timestamp getTimestamp() {
        return _timestamp;
    }

    /**
     *
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        _timestamp = timestamp;
    }

    /**
     *
     * @return
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
        jsonObject.put(Thermostat.THERMOSTAT_MODE, mode);
        jsonObject.put(Thing.THING_ENABLED, _isEnabled);
        jsonObject.put(Thing.THING_READONLY, _isReadonly);
        jsonObject.put(Thing.THING_TIMESTAMP, tstamp.toString());
        return jsonObject.toJSONString();
    }

    /**
     *
     */
    public static class Builder {
        private ThermostatControlPacket _packet;

        /**
         *
         */
        public Builder() {
            _packet = new ThermostatControlPacket();
        }

        /**
         *
         * @param id
         */
        public Builder setThingID(int id) {
            _packet.setID(id);
            return this;
        }

        /**
         *
         * @param clientID
         */
        public Builder setClientID(String clientID) {
            _packet.setClientID(clientID);
            return this;
        }

        /**
         *
         * @param mode
         */
        public Builder setMode(ThermostatMode mode) {
            _packet.setMode(mode);
            return this;
        }

        /**
         *
         * @param enabled
         */
        public Builder setEnabled(boolean enabled) {
            _packet.setEnabled(enabled);
            return this;
        }

        /**
         *
         * @param readonly
         */
        public Builder setReadonly(boolean readonly) {
            _packet.setReadonly(readonly);
            return this;
        }

        /**
         *
         * @param timestamp
         */
        public Builder setTimestamp(Timestamp timestamp) {
            _packet.setTimestamp(timestamp);
            return this;
        }

        /**
         *
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
     *
     * @param jsonString
     * @return
     * @throws ThingParseException
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

            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            ThermostatMode mode = ThermostatMode.OFF.getType((int)(long)jsonObject.get(Thermostat.THERMOSTAT_MODE));
            boolean enable = (boolean)jsonObject.get(Thing.THING_ENABLED);
            boolean readonly = (boolean)jsonObject.get(Thing.THING_READONLY);
            Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(HABSystem.SYS_TIMESTAMP));

            return new Builder()
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
