package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class DoorStatusPacketTest {
    @Test
    public void setThingID() {
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setThingID(1);
        assertEquals(1, packet.getThingID());
    }

    @Test
    public void setClientID() {
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void setState() {
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setState(DoorState.CLOSED);
        assertEquals(DoorState.CLOSED, packet.getState());
    }

    @Test
    public void setEnabled() {
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void setReadonly() {
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void setLocked() {
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setLocked(true);
        assertTrue(packet.isLocked());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DoorStatusPacket packet = new DoorStatusPacket();
        packet.setThingID(1);
        packet.setClientID("foo");
        packet.setTimestamp(tstamp);
        packet.setLocked(true);
        packet.setReadonly(false);
        packet.setEnabled(true);
        packet.setState(DoorState.CLOSED);

        String expected = "{\"readonly\":false,\"lock_enabled\":true,\"id\":1,\"state\":" + DoorState.CLOSED.getValue() +
                ",\"type\":" + ThingType.DOOR.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":" +
                "\"" + tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"readonly\":false,\"lock_enabled\":true,\"id\":1,\"state\":" + DoorState.CLOSED.getValue() +
                ",\"type\":" + ThingType.DOOR.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":" +
                "\"" + tstamp.toString() + "\"}";
        try {
            DoorStatusPacket packet = DoorStatusPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(1, packet.getThingID());
            assertEquals("foo", packet.getClientID());
            assertEquals(tstamp, packet.getTimestamp());
            assertEquals(DoorState.CLOSED, packet.getState());
            assertTrue(packet.isLocked());
            assertTrue(packet.isEnabled());
            assertFalse(packet.isReadonly());
        }
        catch (ThingParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPacketBuilder() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DoorStatusPacket packet = new DoorStatusPacket.Builder()
                .setClientID("foo")
                .setThingID(1)
                .setEnabled(true)
                .setLocked(true)
                .setReadonly(false)
                .setState(DoorState.CLOSED)
                .setTimestamp(tstamp)
                .build();
        assertEquals(1, packet.getThingID());
        assertEquals("foo", packet.getClientID());
        assertEquals(tstamp, packet.getTimestamp());
        assertEquals(DoorState.CLOSED, packet.getState());
        assertTrue(packet.isLocked());
        assertTrue(packet.isEnabled());
        assertFalse(packet.isReadonly());
    }
}