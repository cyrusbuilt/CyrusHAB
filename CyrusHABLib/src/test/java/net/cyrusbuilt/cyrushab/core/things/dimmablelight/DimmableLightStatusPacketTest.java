package net.cyrusbuilt.cyrushab.core.things.dimmablelight;

import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class DimmableLightStatusPacketTest {

    @Test
    public void setThingID() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setThingID(1);
        assertEquals(1, packet.getThingID());
    }

    @Test
    public void setClientID() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void setLevel() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setLevel(100);
        assertEquals(100, packet.getLevel());
    }

    @Test
    public void setMinLevel() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setMinLevel(0);
        assertEquals(0, packet.getMinLevel());
    }

    @Test
    public void setMaxLevel() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setMaxLevel(255);
        assertEquals(255, packet.getMaxLevel());
    }

    @Test
    public void setEnabled() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void setReadonly() {
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket();
        packet.setThingID(1);
        packet.setClientID("foo");
        packet.setTimestamp(tstamp);
        packet.setLevel(100);
        packet.setMinLevel(0);
        packet.setMaxLevel(255);
        packet.setReadonly(false);
        packet.setEnabled(true);

        String expected = "{\"readonly\":false,\"level\":100,\"max_level\":255,\"id\":1,\"type\":" +
                ThingType.DIMMABLE_LIGHT.getValue() + ",\"min_level\":0,\"client_id\":\"foo\",\"enabled\":true," +
                "\"timestamp\":\"" + tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"readonly\":false,\"level\":100,\"max_level\":255,\"id\":1,\"type\":" +
                ThingType.DIMMABLE_LIGHT.getValue() + ",\"min_level\":0,\"client_id\":\"foo\",\"enabled\":true," +
                "\"timestamp\":\"" + tstamp.toString() + "\"}";
        try {
            DimmableLightStatusPacket packet = DimmableLightStatusPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(1, packet.getThingID());
            assertEquals("foo", packet.getClientID());
            assertEquals(100, packet.getLevel());
            assertEquals(0, packet.getMinLevel());
            assertEquals(255, packet.getMaxLevel());
            assertEquals(tstamp, packet.getTimestamp());
            assertEquals(ThingType.DIMMABLE_LIGHT, packet.getType());
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
        DimmableLightStatusPacket packet = new DimmableLightStatusPacket.Builder()
                .setThingID(1)
                .setClientID("foo")
                .setLevel(100)
                .setMinLevel(0)
                .setMaxLevel(255)
                .setTimestamp(tstamp)
                .setEnabled(true)
                .setReadonly(false)
                .build();
        assertEquals(1, packet.getThingID());
        assertEquals("foo", packet.getClientID());
        assertEquals(100, packet.getLevel());
        assertEquals(0, packet.getMinLevel());
        assertEquals(255, packet.getMaxLevel());
        assertEquals(tstamp, packet.getTimestamp());
        assertEquals(ThingType.DIMMABLE_LIGHT, packet.getType());
        assertTrue(packet.isEnabled());
        assertFalse(packet.isReadonly());
    }
}