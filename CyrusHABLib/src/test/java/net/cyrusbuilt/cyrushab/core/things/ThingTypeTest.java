package net.cyrusbuilt.cyrushab.core.things;

import org.junit.Test;

import static org.junit.Assert.*;

public class ThingTypeTest {
    @Test
    public void getType() {
        assertEquals(ThingType.THERMOSTAT, ThingType.UNKNOWN.getType(1));
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = ThingType.THERMOSTAT.getValue();
        assertEquals(expected, actual);
    }
}