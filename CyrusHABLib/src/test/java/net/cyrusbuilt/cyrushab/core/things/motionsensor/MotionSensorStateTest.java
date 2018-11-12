package net.cyrusbuilt.cyrushab.core.things.motionsensor;

import org.junit.Test;

import static org.junit.Assert.*;

public class MotionSensorStateTest {
    @Test
    public void getType() {
        MotionSensorState expected = MotionSensorState.TRIPPED;
        MotionSensorState actual = MotionSensorState.UNKNOWN.getType(1);
        assertEquals(expected, actual);
    }

    @Test
    public void getValue() {
        int expected = 1;
        int actual = MotionSensorState.TRIPPED.getValue();
        assertEquals(expected, actual);
    }
}