package net.cyrusbuilt.cyrushab.core.things;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Basic thing utilities.
 */
public final class BasicThingUtils {
    /**
     * private ctor since this is a static class.
     */
    private BasicThingUtils() {}

    /**
     * Parses a {@link MinimalThingInfo} object from the specified JSON string.
     * @param jsonString The JSON string to parse.
     * @return null if the specified JSON string is null or empty; Otherwise, a {@link MinimalThingInfo} object
     * populated with the attributes from the specified JSON.
     * @throws ThingParseException if an error occurs while parsing the JSON string (ie. invalid format or missing
     * attribute).
     */
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
