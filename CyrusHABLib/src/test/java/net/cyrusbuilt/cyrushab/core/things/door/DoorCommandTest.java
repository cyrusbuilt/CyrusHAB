package net.cyrusbuilt.cyrushab.core.things.door;

import org.junit.Test;

import static org.junit.Assert.*;

public class DoorCommandTest {

    @Test
    public void getType() {
        DoorCommand expected = DoorCommand.CLOSE;
        DoorCommand actual = DoorCommand.UNKNOWN.getType(1);
        assertEquals(expected, actual);
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = DoorCommand.CLOSE.getValue();
        assertEquals(expected, actual);
    }
}