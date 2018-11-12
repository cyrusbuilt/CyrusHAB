package net.cyrusbuilt.cyrushab.core.things.motionsensor;

import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import static org.junit.Assert.*;

public class MotionSensorTest {
    @Test
    public void setThingID() {
        MotionSensor sensor = new MotionSensor() {};
        sensor.setThingID(1);
        assertEquals(1, sensor.id());
    }

    @Test
    public void setClientID() {
        MotionSensor sensor = new MotionSensor() {};
        sensor.setClientID("foo");
        assertEquals("foo", sensor.clientID());
    }

    @Test
    public void setName() {
        MotionSensor sensor = new MotionSensor() {};
        sensor.setName("bar");
        assertEquals("bar", sensor.name());
    }

    @Test
    public void setTag() {
        Object obj = new Object();
        MotionSensor sensor = new MotionSensor() {};
        sensor.setTag(obj);
        assertEquals(obj, sensor.tag());
    }

    @Test
    public void type() {
        MotionSensor sensor = new MotionSensor() {};
        assertEquals(ThingType.MOTION_SENSOR, sensor.type());
    }

    @Test
    public void isReadonly() {
        MotionSensor sensor = new MotionSensor() {};
        assertTrue(sensor.isReadonly());
    }

    @Test
    public void notifyListeners() {
        MotionSensor sensor = new MotionSensor() {};
        try {
            sensor.addListener(event -> {
                assertEquals(MotionSensorState.IDLE, event.getOldState());
                assertEquals(MotionSensorState.TRIPPED, event.getNewState());
            });

            sensor.notifyListeners(new MotionSensor.MotionSensorEvent(MotionSensorState.IDLE, MotionSensorState.TRIPPED));
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void setState() {
        MotionSensor sensor = new MotionSensor() {};
        try {
            sensor.setState(MotionSensorState.TRIPPED);
            assertEquals(MotionSensorState.TRIPPED, sensor.getState());
            assertTrue(sensor.isTripped());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void setEnabled() {
        MotionSensor sensor = new MotionSensor() {};
        try {
            sensor.setEnabled(true);
            assertTrue(sensor.isEnabled());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void dispose() {
        MotionSensor sensor = new MotionSensor() {};
        assertFalse(sensor.isDisposed());
        sensor.dispose();
        assertTrue(sensor.isDisposed());
    }

    @Test
    public void mapFromStatusPacket() {
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket.Builder()
                .setEnabled(true)
                .setState(MotionSensorState.TRIPPED)
                .build();
        MotionSensor sensor = new MotionSensor() {};
        try {
            sensor.mapFromStatusPacket(packet);
            assertTrue(sensor.isEnabled());
            assertTrue(sensor.isTripped());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }
}