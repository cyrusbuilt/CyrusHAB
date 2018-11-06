package net.cyrusbuilt.cyrushab.core.things;

import org.junit.Test;

import static org.junit.Assert.*;

public class MinimalThingInfoTest {
    @Test
    public void ctorTest() {
        MinimalThingInfo thing = new MinimalThingInfo(1, "test", ThingType.SWITCH);

        int expected = 1;
        int actual = thing.getID();
        assertEquals(expected, actual);
        assertEquals("test", thing.getClientID());
        assertEquals(ThingType.SWITCH, thing.getThingType());
    }
}