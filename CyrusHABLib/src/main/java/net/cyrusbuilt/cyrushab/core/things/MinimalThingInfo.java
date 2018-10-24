package net.cyrusbuilt.cyrushab.core.things;

import org.apache.commons.lang3.StringUtils;

public class MinimalThingInfo {
    private int _id = -1;
    private String _clientID = StringUtils.EMPTY;
    private ThingType _type = ThingType.UNKNOWN;

    public MinimalThingInfo(int id, String clientID, ThingType type) {
        _id = id;
        _clientID = clientID;
        _type = type;
    }

    public int getID() {
        return _id;
    }

    public String getClientID() {
        return _clientID;
    }

    public ThingType getThingType() {
        return _type;
    }
}
