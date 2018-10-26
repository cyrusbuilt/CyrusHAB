package net.cyrusbuilt.cyrushab.core.things.dimmablelight;

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
public class DimmableLightStatusPacket {
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
   public void setClientID(String clientID) {
       _clientID = clientID;
   }

    /**
     *
     * @return
     */
   public int getLevel() {
       return _level;
   }

    /**
     *
     * @param level
     */
   public void setLevel(int level) {
       _level = level;
   }

    /**
     *
     * @return
     */
   public int getMinLevel() {
       return _minLevel;
   }

    /**
     *
     * @param minLevel
     */
   public void setMinLevel(int minLevel) {
       _minLevel = minLevel;
   }

    /**
     *
     * @return
     */
   public int getMaxLevel() {
       return _maxLevel;
   }

    /**
     *
     * @param maxLevel
     */
   public void setMaxLevel(int maxLevel) {
       _maxLevel = maxLevel;
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
     *
     */
   public static class Builder {
       private DimmableLightStatusPacket _packet;

        /**
         *
         */
       public Builder() {
           _packet = new DimmableLightStatusPacket();
       }

        /**
         *
         * @param id
         */
       public Builder setThingID(int id) {
           _packet.setThingID(id);
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
         * @param level
         */
       public Builder setLevel(int level) {
           _packet.setLevel(level);
           return this;
       }

        /**
         *
         * @param minLevel
         */
       public Builder setMinLevel(int minLevel) {
           _packet.setMinLevel(minLevel);
           return this;
       }

        /**
         *
         * @param maxLevel
         */
       public Builder setMaxLevel(int maxLevel) {
           _packet.setMaxLevel(maxLevel);
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
     *
     * @param jsonString
     * @return
     * @throws ThingParseException
     */
   @Nullable
   public DimmableLightStatusPacket fromJsonString(String jsonString) throws ThingParseException {
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
