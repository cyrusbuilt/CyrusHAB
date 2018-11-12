package net.cyrusbuilt.cyrushab.core.things.switches;

import org.junit.Test;

import static org.junit.Assert.*;

public class SwitchStateTest {
    @Test
    public void getType() {
        SwitchState expected = SwitchState.OFF;
        SwitchState actual = SwitchState.ON.getType(0);
        assertEquals(expected, actual);
    }

    @Test
    public void getValue() {
        int expected = 0;
        int actual = SwitchState.OFF.getValue();
        assertEquals(expected, actual);
    }
}