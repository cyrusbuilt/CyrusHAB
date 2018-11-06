package net.cyrusbuilt.cyrushab.core.things.thermostat;

import org.junit.Test;

import static org.junit.Assert.*;

public class ThermostatStateTest {

    @Test
    public void getType() {
        assertEquals(ThermostatState.HEATING, ThermostatState.UNKNOWN.getType(1));
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = ThermostatState.HEATING.getValue();
        assertEquals(expected, actual);
    }
}