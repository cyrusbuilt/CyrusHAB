package net.cyrusbuilt.cyrushab.core.things;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class BasicThingUtils {
    private BasicThingUtils() {}

    @Nullable
    public static MinimalThingInfo parseMinimalThingInfoFromJson(String jsonString) throws ThingParseException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject)obj;
            Object idObj = jsonObject.get(Thing.THING_ID);
            int id = 0;
            if (idObj != null) {
                // System control/status packets do not contain an ID field.
                id = (int)(long)idObj;
            }
            String clientID = (String)jsonObject.get(Thing.THING_CLIENT_ID);
            ThingType type = ThingType.UNKNOWN.getType((int)(long)jsonObject.get(Thing.THING_TYPE));
            return new MinimalThingInfo(id, clientID, type);
        }
        catch (Exception e) {
            throw new ThingParseException(e);
        }
    }
}
