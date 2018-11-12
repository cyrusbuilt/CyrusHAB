package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import static org.junit.Assert.*;

public class SwitchTest {
    @Test
    public void setThingID() {
        Switch sw = new Switch() {};
        sw.setThingID(1);
        assertEquals(1, sw.id());
    }

    @Test
    public void setClientID() {
        Switch sw = new Switch() {};
        sw.setClientID("foo");
        assertEquals("foo", sw.clientID());
    }

    @Test
    public void setName() {
        Switch sw = new Switch() {};
        sw.setName("bar");
        assertEquals("bar", sw.name());
    }

    @Test
    public void setTag() {
        Object obj = new Object();
        Switch sw = new Switch() {};
        sw.setTag(obj);
        assertEquals(obj, sw.tag());
    }

    @Test
    public void type() {
        Switch sw = new Switch() {};
        assertEquals(ThingType.SWITCH, sw.type());
    }

    @Test
    public void setIsReadonly() {
        Switch sw = new Switch() {};
        sw.setIsReadonly(true);
        assertTrue(sw.isReadonly());
    }

    @Test
    public void setEnabled() {
        Switch sw = new Switch() {};
        try {
            sw.setEnabled(true);
            assertTrue(sw.isEnabled());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void notifyListeners() {
        Switch sw = new Switch() {};
        try {
            sw.addListener(event -> {
                assertEquals("foo", event.name());
                assertEquals(SwitchState.OFF, event.oldState());
                assertEquals(SwitchState.ON, event.newState());
            });

            sw.notifyListeners(new Switch.SwitchEvent(SwitchState.OFF, SwitchState.ON, "foo"));
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void dispose() {
        Switch sw = new Switch() {};
        assertFalse(sw.isDisposed());
        sw.dispose();
        assertTrue(sw.isDisposed());
    }

    @Test
    public void setState() {
        Switch sw = new Switch() {};
        try {
            sw.setState(SwitchState.ON);
            assertEquals(SwitchState.ON, sw.state());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void isOn() {
        Switch sw = new Switch() {};
        try {
            sw.setState(SwitchState.ON);
            assertTrue(sw.isOn());
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void isOff() {
        Switch sw = new Switch() {};
        assertTrue(sw.isOff());
    }

    @Test
    public void mapFromStatusPacket() {
        SwitchStatusPacket packet = new SwitchStatusPacket.Builder()
                .setEnabled(true)
                .setReadonly(true)
                .setState(SwitchState.ON)
                .build();
        Switch sw = new Switch() {};
        try {
            sw.mapFromStatusPacket(packet);
            assertTrue(sw.isEnabled());
            assertTrue(sw.isReadonly());
            assertEquals(SwitchState.ON, sw.state());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }
}