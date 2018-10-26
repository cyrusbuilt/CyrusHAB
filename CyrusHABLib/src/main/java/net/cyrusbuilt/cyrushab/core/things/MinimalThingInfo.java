package net.cyrusbuilt.cyrushab.core.things;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents the most basic attributes of a "Thing".
 */
public class MinimalThingInfo {
    private int _id = -1;
    private String _clientID = StringUtils.EMPTY;
    private ThingType _type = ThingType.UNKNOWN;

    /**
     * Constructs a new instance of {@link MinimalThingInfo} with the ID, client ID, and type.
     * @param id The Thing ID.
     * @param clientID The client ID.
     * @param type The thing type.
     */
    public MinimalThingInfo(int id, String clientID, ThingType type) {
        _id = id;
        _clientID = clientID;
        _type = type;
    }

    /**
     * Gets the Thing ID.
     * @return The ID.
     */
    public int getID() {
        return _id;
    }

    /**
     * Gets the client ID.
     * @return The client ID.
     */
    public String getClientID() {
        return _clientID;
    }

    /**
     * Gets the Thing type.
     * @return The type.
     */
    public ThingType getThingType() {
        return _type;
    }
}
