package net.cyrusbuilt.cyrushab.core.telemetry;

import org.junit.Test;

import static org.junit.Assert.*;

public class SystemCommandTest {

    @Test
    public void getType() {
        SystemCommand expected = SystemCommand.DISABLE;
        SystemCommand actual = SystemCommand.UNKNOWN.getType(1);
        assertEquals(expected, actual);
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = SystemCommand.DISABLE.getValue();
        assertEquals(expected, actual);
    }
}