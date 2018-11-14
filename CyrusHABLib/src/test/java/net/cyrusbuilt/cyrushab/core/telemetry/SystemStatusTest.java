package net.cyrusbuilt.cyrushab.core.telemetry;

import org.junit.Test;

import static org.junit.Assert.*;

public class SystemStatusTest {

    @Test
    public void getType() {
        SystemStatus expected = SystemStatus.DISABLED;
        SystemStatus actual = SystemStatus.UNKNOWN.getType(1);
        assertEquals(expected, actual);
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = SystemStatus.DISABLED.getValue();
        assertEquals(expected, actual);
    }
}