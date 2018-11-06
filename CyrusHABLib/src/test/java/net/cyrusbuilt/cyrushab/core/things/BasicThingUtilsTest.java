package net.cyrusbuilt.cyrushab.core.things;

import org.junit.Test;

import static org.junit.Assert.*;

public class BasicThingUtilsTest {
    @Test
    public void parseMinimalThingInfoFromJson() {
        String testStr = "{\"id\":1,\"client_id\":\"test\",\"type\":1}";
        try {
            MinimalThingInfo result = BasicThingUtils.parseMinimalThingInfoFromJson(testStr);
            assertNotNull(result);

            assertEquals(1, result.getID());
            assertEquals("test", result.getClientID());
            assertEquals(ThingType.THERMOSTAT, result.getThingType());
        }
        catch (ThingParseException ex) {
            fail();
        }
    }
}