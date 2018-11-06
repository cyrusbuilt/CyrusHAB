package net.cyrusbuilt.cyrushab.core.things.thermostat;

import org.junit.Test;

import static org.junit.Assert.*;

public class ThermostatModeTest {

    @Test
    public void getType() {
        assertEquals(ThermostatMode.COOL, ThermostatMode.OFF.getType(2));
    }

    @Test
    public void getValue() {
        int expected = 2;
        int actual = ThermostatMode.COOL.getValue();
        assertEquals(expected, actual);
    }
}