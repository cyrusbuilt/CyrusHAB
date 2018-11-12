package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import static org.junit.Assert.*;

public class DoorTest {

    @Test
    public void setThingID() {
        Door door = new Door() {};
        door.setThingID(1);
        assertEquals(1, door.id());
    }

    @Test
    public void setClientID() {
        Door door = new Door() {};
        door.setClientID("foo");
        assertEquals("foo", door.clientID());
    }

    @Test
    public void setName() {
        Door door = new Door() {};
        door.setName("bar");
        assertEquals("bar", door.name());
    }

    @Test
    public void setTag() {
        Object obj = new Object();
        Door door = new Door() {};
        door.setTag(obj);
        assertEquals(obj, door.tag());
    }

    @Test
    public void type() {
        Door door = new Door() {};
        assertEquals(ThingType.DOOR, door.type());
    }

    @Test
    public void setIsReadonly() {
        Door door = new Door() {};
        door.setIsReadonly(true);
        assertTrue(door.isReadonly());
    }

    @Test
    public void notifyDoorStateListeners() {
        Door door = new Door() {};
        try {
            door.addListener(new Door.OnDoorStateChangedListener() {
                @Override
                public void onDoorStateChanged(Door.DoorEvent event) {
                    assertEquals(DoorState.OPEN, event.oldState());
                    assertEquals(DoorState.CLOSED, event.newState());
                }

                @Override
                public void onDoorLockChanged(Door.DoorEvent event) {
                    assertTrue(event.lockEnabled());
                }
            });

            door.notifyDoorStateListeners(new Door.DoorEvent(DoorState.OPEN, DoorState.CLOSED, false));
            door.notifyDoorLockListeners(new Door.DoorEvent(DoorState.OPEN, DoorState.CLOSED, true));
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void setState() {
        Door door = new Door() {};
        try {
            door.setState(DoorState.CLOSED);
            assertEquals(DoorState.CLOSED, door.getState());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void open() {
        Door door = new Door() {};
        try {
            door.open();
            assertTrue(door.isOpen());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void close() {
        Door door = new Door() {};
        try {
            door.close();
            assertTrue(door.isClosed());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void setEnabled() {
        Door door = new Door() {};
        try {
            door.setEnabled(true);
            assertTrue(door.isEnabled());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void setLocked() {
        Door door = new Door() {};
        try {
            door.setLocked(true);
            assertTrue(door.isLocked());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void dispose() {
        Door door = new Door() {};
        assertFalse(door.isDisposed());
        door.dispose();
        assertTrue(door.isDisposed());
    }

    @Test
    public void mapFromStatusPacket() {
        Door door = new Door() {};
        DoorStatusPacket packet = new DoorStatusPacket.Builder()
                .setState(DoorState.CLOSED)
                .setEnabled(true)
                .setReadonly(false)
                .setLocked(true)
                .build();
        try {
            door.mapFromStatusPacket(packet);
            assertEquals(DoorState.CLOSED, door.getState());
            assertTrue(door.isEnabled());
            assertTrue(door.isLocked());
            assertFalse(door.isReadonly());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }
}