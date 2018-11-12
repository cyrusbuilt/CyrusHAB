package net.cyrusbuilt.cyrushab.core.things.door;

import org.junit.Test;

import static org.junit.Assert.*;

public class DoorStateTest {

    @Test
    public void getType() {
        DoorState expected = DoorState.CLOSED;
        DoorState actual = DoorState.UNKNOWN.getType(1);
        assertEquals(expected, actual);
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = DoorState.CLOSED.getValue();
        assertEquals(expected, actual);
    }
}