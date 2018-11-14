package net.cyrusbuilt.cyrushab.core.things.dimmablelight;

import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import static org.junit.Assert.*;

public class DimmableLightTest {
    @Test
    public void setThingID() {
        DimmableLight light = new DimmableLight(0, 255) {};
        light.setThingID(1);
        assertEquals(1, light.id());
    }

    @Test
    public void setClientID() {
        DimmableLight light = new DimmableLight(0, 255) {};
        light.setClientID("foo");
        assertEquals("foo", light.clientID());
    }

    @Test
    public void setName() {
        DimmableLight light = new DimmableLight(0, 255) {};
        light.setName("foo");
        assertEquals("foo", light.name());
    }

    @Test
    public void setTag() {
        Object obj = new Object();
        DimmableLight light = new DimmableLight(0, 255) {};
        light.setTag(obj);
        assertEquals(obj, light.tag());
    }

    @Test
    public void type() {
        DimmableLight light = new DimmableLight(0, 255) {};
        assertEquals(ThingType.DIMMABLE_LIGHT, light.type());
    }

    @Test
    public void setIsReadonly() {
        DimmableLight light = new DimmableLight(0, 255) {};
        light.setIsReadonly(true);
        assertTrue(light.isReadonly());
    }

    @Test
    public void setLevel() {
        DimmableLight light = new DimmableLight(0, 255) {};
        try {
            light.setLevel(100);
            assertEquals(100, light.level());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void turnOn() {
        DimmableLight light = new DimmableLight(0, 255) {};
        try {
            light.turnOn();
            assertTrue(light.isOn());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void turnOff() {
        DimmableLight light = new DimmableLight(0, 255) {};
        try {
            light.turnOff();
            assertTrue(light.isOff());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void setEnabled() {
        DimmableLight light = new DimmableLight(0, 255) {};
        try {
            light.setEnabled(true);
            assertTrue(light.isEnabled());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void notifyListeners() {
        DimmableLight light = new DimmableLight(0, 255) {};
        try {
            light.addListener(event -> {
                assertEquals(0, event.minLevel());
                assertEquals(255, event.maxLevel());
                assertEquals(100, event.level());
                assertTrue(event.isOn());
            });

            light.notifyListeners(new DimmableLight.DimmableEvent(100, 0, 255, true));
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void dispose() {
        DimmableLight light = new DimmableLight(0, 255) {};
        assertFalse(light.isDisposed());
        light.dispose();
        assertTrue(light.isDisposed());
    }

    @Test
    public void getLevelPercentage() {
        DimmableLight light = new DimmableLight(0, 255) {};
        try {
            light.setLevel(100);
            int expected = 39;
            int actual = light.getLevelPercentage();
            assertEquals(expected, actual);
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void mapFromStatusPacket() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket.Builder()
                .setReadonly(false)
                .setEnabled(true)
                .setLevel(100)
                .setMinLevel(0)
                .setMaxLevel(255)
                .build();
        DimmableLight light = new DimmableLight(10, 200) {};
        try {
            light.mapFromStatusPacket(packet);
            assertEquals(100, light.level());
            assertEquals(0, light.minLevel());
            assertEquals(255, light.maxLevel());
            assertTrue(light.isEnabled());
            assertTrue(light.isOn());
            assertFalse(light.isReadonly());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }
}