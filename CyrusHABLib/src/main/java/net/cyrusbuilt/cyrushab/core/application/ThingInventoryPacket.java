package net.cyrusbuilt.cyrushab.core.application;

import net.cyrusbuilt.cyrushab.core.things.Packet;
import net.cyrusbuilt.cyrushab.core.things.Thing;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a packet containing an inventory of all Things for transmission over MQTT.
 */
public class ThingInventoryPacket implements Packet {
    private String _clientID;
    private String _hostID;
    private List<Thing> _inventory;
    private Timestamp _timestamp;

    /**
     * Constructs a new instance of {@link ThingInventoryPacket}.
     */
    public ThingInventoryPacket() {}

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
     * Gets the host ID (the client ID of the host system).
     * @return The host ID.
     */
    public String getHostID() {
        return _hostID;
    }

    /**
     * Sets the host ID (the client ID of the host system).
     * @param hostID The host ID.
     */
    public void setHostID(String hostID) {
        _hostID = hostID;
    }

    /**
     * Gets the Thing inventory.
     * @return The inventory.
     */
    public List<Thing> getThingInventory() {
        return _inventory;
    }

    /**
     * Sets the Thing inventory.
     * @param inventory The inventory.
     */
    public void setThingInventory(List<Thing> inventory) {
        _inventory = inventory;
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
     * generated. If the timestamp was not specified, then the current local date/time will be used. If a host ID was
     * not specified, then the default will be used. If not inventory was provided, then an empty list will be used.
     * All other values will be default unless set otherwise.
     * @return The constructed JSON structure converted to string. Example:
     * {
     *      "client_id": "some_id",
     *      "host_id": "hab_system_1",
     *      "timestamp": "2018-10-17 15:14:51",
     *      "things": [
     *          "thing": {
     *              "id": 0,
     *              "client_id": "light_1",
     *              "type": 2,
     *              "name": "living room light",
     *              "readonly": false,
     *              "enabled": true
     *          },
     *          "thing": {
     *              "id": 1,
     *              "client_id": "thermostat_1",
     *              "type": 1,
     *              "name": "main thermostat",
     *              "readonly": false,
     *              "enabled": true
     *          }
     *      ]
     * }
     */
    @Override
    public String toJsonString() {
        String clientID = _clientID;
        if (StringUtils.isBlank(clientID)) {
            clientID = MqttClient.generateClientId();
        }

        String hostID = _hostID;
        if (StringUtils.isBlank(hostID)) {
            hostID = HABApp.APP_DEFAULT_HOST_ID;
        }

        List<Thing> inventory = _inventory;
        if (inventory == null) {
            inventory = new ArrayList<>();
        }

        Timestamp tstamp = _timestamp;
        if (tstamp == null) {
            tstamp = Timestamp.valueOf(LocalDateTime.now());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HABApp.APP_CLIENT_ID, clientID);
        jsonObject.put(HABApp.APP_HOST_ID, hostID);
        jsonObject.put(HABApp.APP_TIMESTAMP, tstamp.toString());

        JSONArray thingList = new JSONArray();
        for (Thing thing : inventory) {
            JSONObject obj = new JSONObject();
            obj.put(Thing.THING_ID, thing.id());
            obj.put(Thing.THING_CLIENT_ID, thing.clientID());
            obj.put(Thing.THING_TYPE, thing.type().getValue());
            obj.put(Thing.THING_NAME, thing.name());
            obj.put(Thing.THING_READONLY, thing.isReadonly());
            obj.put(Thing.THING_ENABLED, thing.isEnabled());
            thingList.add(HABApp.APP_THING + ": " + obj.toJSONString());
        }

        jsonObject.put(HABApp.APP_THINGS, thingList);
        return jsonObject.toJSONString();
    }

    /**
     * A Builder class for {@link ThingInventoryPacket} objects. Allows easier control over all the flags, as well as
     * help constructing a typical packet. If any of the flags are not set, a default value will be used.
     */
    public static class Builder implements Packet.Builder<ThingInventoryPacket> {
        private ThingInventoryPacket _packet;

        /**
         * Constructs a new instance of {@link Builder}.
         */
        public Builder() {
            _packet = new ThingInventoryPacket();
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
         * Sets the host ID (the host system's client ID).
         * @param hostID The host ID.
         */
        public Builder setHostID(String hostID) {
            _packet.setHostID(hostID);
            return this;
        }

        /**
         * Sets the Thing inventory.
         * @param inventory The inventory.
         */
        public Builder setThingInventory(List<Thing> inventory) {
            _packet.setThingInventory(inventory);
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
         * Combine all of the options that have been set and return a new {@link ThingInventoryPacket}.
         */
        @Override
        public ThingInventoryPacket build() {
            if (StringUtils.isBlank(_packet.getClientID())) {
                _packet.setClientID(MqttClient.generateClientId());
            }

            if (StringUtils.isBlank(_packet.getHostID())) {
                _packet.setHostID(HABApp.APP_DEFAULT_HOST_ID);
            }

            if (_packet.getTimestamp() == null) {
                _packet.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            }

            if (_packet.getThingInventory() == null) {
                _packet.setThingInventory(new ArrayList<>());
            }

            return _packet;
        }
    }
}
