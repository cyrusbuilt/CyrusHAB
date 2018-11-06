package net.cyrusbuilt.cyrushab.core.things.dimmablelight;

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
 * Represents a Dimmable light status packet for transmission over MQTT.
 */
public class DimmableLightStatusPacket implements Packet {
   private int _id = -1;
   private String _clientID = StringUtils.EMPTY;
   private int _level = 0;
   private int _maxLevel = 0;
   private int _minLevel = 0;
   private boolean _isEnabled = false;
   private boolean _isReadonly = false;
   private Timestamp _timestamp;

    /**
     * Constructs a new instance of {@link DimmableLightStatusPacket}.
     */
   public DimmableLightStatusPacket() {}

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
     * Gets the current light level.
     * @return The light level.
     */
   public int getLevel() {
       return _level;
   }

    /**
     * Sets the light level.
     * @param level The light level.
     */
   public void setLevel(int level) {
       _level = level;
   }

    /**
     * Gets the minimum level.
     * @return The minimum level.
     */
   public int getMinLevel() {
       return _minLevel;
   }

    /**
     * Sets the minimum light level.
     * @param minLevel The minimum level.
     */
   public void setMinLevel(int minLevel) {
       _minLevel = minLevel;
   }

    /**
     * Gets the maximum light level.
     * @return The maximum level.
     */
   public int getMaxLevel() {
       return _maxLevel;
   }

    /**
     * Sets the maximum light level.
     * @param maxLevel The max level.
     */
   public void setMaxLevel(int maxLevel) {
       _maxLevel = maxLevel;
   }

    /**
     * Gets whether or not the light is enabled.
     * @return true if enabled; Otherwise, false.
     */
   public boolean isEnabled() {
       return _isEnabled;
   }

    /**
     * Sets whether or not the light is enabled.
     * @param enabled Set true to enable.
     */
   public void setEnabled(boolean enabled) {
       _isEnabled = enabled;
   }

    /**
     * Gets whether or not the light is read-only.
     * @return true if read-only; Otherwise, false.
     */
   public boolean isReadonly() {
       return _isReadonly;
   }

    /**
     * Sets whether or not the light is read-only.
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
     * Builds a JSON string representation of the status packet data. If client ID was not specified, then one will be
     * randomly generated. If the timestamp was not specified, then the current local date/time will be used. All other
     * values will be default unless set otherwise.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *     "id": 5,
     *     "client_id": "dimmable_light_1",
     *     "level": 130,
     *     "min_level": 0,
     *     "max_level": 255,
     *     "type": 3,
     *     "enabled": true,
     *     "readonly": false,
     *     "timestamp": "2018-10-27 09:30:27.49"
     * }
     */
    @Override
   public String toJsonString() {
       String clientID = _clientID;
       if (StringUtils.isBlank(clientID)) {
           clientID = MqttClient.generateClientId();
       }

       int type = ThingType.SWITCH.getValue();
       Timestamp tstamp = _timestamp;
       if (tstamp == null) {
           tstamp = Timestamp.valueOf(LocalDateTime.now());
       }

       JSONObject jsonObject = new JSONObject();
       jsonObject.put(Thing.THING_ID, _id);
       jsonObject.put(Thing.THING_CLIENT_ID, clientID);
       jsonObject.put(DimmableLight.DIMMABLE_LEVEL, _level);
       jsonObject.put(DimmableLight.DIMMABLE_MIN_LEVEL, _minLevel);
       jsonObject.put(DimmableLight.DIMMABLE_MAX_LEVEL, _maxLevel);
       jsonObject.put(Thing.THING_TYPE, type);
       jsonObject.put(Thing.THING_ENABLED, _isEnabled);
       jsonObject.put(Thing.THING_READONLY, _isReadonly);
       jsonObject.put(Thing.THING_TIMESTAMP, tstamp);
       return jsonObject.toJSONString();
   }

    /**
     * Builder class for {@link DimmableLightStatusPacket} objects. Allows easier control over all the flags, as well as
     * help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
   public static class Builder implements Packet.Builder<DimmableLightStatusPacket> {
       private DimmableLightStatusPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
       public Builder() {
           _packet = new DimmableLightStatusPacket();
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
         * Sets the light level.
         * @param level The level.
         */
       public Builder setLevel(int level) {
           _packet.setLevel(level);
           return this;
       }

        /**
         * Sets the minimum light level.
         * @param minLevel The minimum level.
         */
       public Builder setMinLevel(int minLevel) {
           _packet.setMinLevel(minLevel);
           return this;
       }

        /**
         * Sets the maximum light level.
         * @param maxLevel The maximum level.
         */
       public Builder setMaxLevel(int maxLevel) {
           _packet.setMaxLevel(maxLevel);
           return this;
       }

        /**
         * Sets whether or not the light is enabled.
         * @param enabled Set true to enable.
         */
       public Builder setEnabled(boolean enabled) {
           _packet.setEnabled(enabled);
           return this;
       }

        /**
         * Sets whether or not the light is read-only.
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
         * Combine all of the options that have been set and return a new {@link DimmableLightStatusPacket}.
         */
        @Override
       public DimmableLightStatusPacket build() {
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
     * Parses a {@link DimmableLightStatusPacket} from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified string is null or empty. Otherwise, a new {@link DimmableLightStatusPacket}
     * populated with the values retrieved from the JSON object structure.
     * @throws ThingParseException if parsing the specified JSON string failed (ie. invalid format).
     */
   @Nullable
   public static DimmableLightStatusPacket fromJsonString(String jsonString) throws ThingParseException {
       if (StringUtils.isBlank(jsonString)) {
           return null;
       }

       JSONParser parser = new JSONParser();
       try {
           Object obj = parser.parse(jsonString);
           JSONObject jsonObject = (JSONObject)obj;
           ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
           if (type != ThingType.DIMMABLE_LIGHT) {
               // This is not a dimmable light status packet.
               throw new ThingParseException("The specified JSON is not for a Dimmable Light type.");
           }

           int id = (int)(long)jsonObject.get(Thing.THING_ID);
           String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
           int level = (int)(long)jsonObject.get(DimmableLight.DIMMABLE_LEVEL);
           int minLevel = (int)(long)jsonObject.get(DimmableLight.DIMMABLE_MIN_LEVEL);
           int maxLevel = (int)(long)jsonObject.get(DimmableLight.DIMMABLE_MAX_LEVEL);
           boolean enable = (boolean)jsonObject.get(Thing.THING_ENABLED);
           boolean readonly = (boolean)jsonObject.get(Thing.THING_READONLY);
           Timestamp tstamp = Timestamp.valueOf((String)jsonObject.get(Thing.THING_TIMESTAMP));

           return new Builder()
                   .setThingID(id)
                   .setClientID(clientID)
                   .setLevel(level)
                   .setMinLevel(minLevel)
                   .setMaxLevel(maxLevel)
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
