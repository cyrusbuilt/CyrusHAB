package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class DoorControlPacketTest {
    @Test
    public void setThingID() {
        DoorControlPacket packet = new DoorControlPacket();
        packet.setThingID(1);
        assertEquals(1, packet.getThingID());
    }

    @Test
    public void setClientID() {
        DoorControlPacket packet = new DoorControlPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void setCommand() {
        DoorControlPacket packet = new DoorControlPacket();
        packet.setCommand(DoorCommand.CLOSE);
        assertEquals(DoorCommand.CLOSE, packet.getCommand());
    }

    @Test
    public void enableLock() {
        DoorControlPacket packet = new DoorControlPacket();
        packet.enableLock(true);
        assertTrue(packet.isLockEnabled());
    }

    @Test
    public void setEnabled() {
        DoorControlPacket packet = new DoorControlPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void setReadonly() {
        DoorControlPacket packet = new DoorControlPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DoorControlPacket packet = new DoorControlPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DoorControlPacket packet = new DoorControlPacket();
        packet.setThingID(1);
        packet.setClientID("foo");
        packet.setTimestamp(tstamp);
        packet.setCommand(DoorCommand.CLOSE);
        packet.setReadonly(false);
        packet.setEnabled(true);
        packet.enableLock(true);

        String expected = "{\"readonly\":false,\"lock_enabled\":true,\"id\":1,\"type\":" + ThingType.DOOR.getValue() +
                ",\"client_id\":\"foo\",\"command\":" + DoorCommand.CLOSE.getValue() + ",\"enabled\":true," +
                "\"timestamp\":\"" + tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"readonly\":false,\"lock_enabled\":true,\"id\":1,\"type\":" + ThingType.DOOR.getValue() +
                ",\"client_id\":\"foo\",\"command\":" + DoorCommand.CLOSE.getValue() + ",\"enabled\":true," +
                "\"timestamp\":\"" + tstamp.toString() + "\"}";
        try {
            DoorControlPacket packet = DoorControlPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(1, packet.getThingID());
            assertEquals("foo", packet.getClientID());
            assertEquals(ThingType.DOOR, packet.getType());
            assertEquals(DoorCommand.CLOSE, packet.getCommand());
            assertEquals(tstamp, packet.getTimestamp());
            assertTrue(packet.isEnabled());
            assertTrue(packet.isLockEnabled());
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
        DoorControlPacket packet = new DoorControlPacket.Builder()
                .setClientID("foo")
                .setThingID(1)
                .setCommand(DoorCommand.CLOSE)
                .setTimestamp(tstamp)
                .setEnabled(true)
                .setLockEnabled(true)
                .setReadonly(false)
                .build();
        assertEquals(1, packet.getThingID());
        assertEquals("foo", packet.getClientID());
        assertEquals(ThingType.DOOR, packet.getType());
        assertEquals(DoorCommand.CLOSE, packet.getCommand());
        assertEquals(tstamp, packet.getTimestamp());
        assertTrue(packet.isEnabled());
        assertTrue(packet.isLockEnabled());
        assertFalse(packet.isReadonly());
    }
}