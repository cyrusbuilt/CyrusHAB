package net.cyrusbuilt.cyrushab.core.telemetry;

import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class SystemStatusPacketTest {

    @Test
    public void testCtor() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SystemStatusPacket packet = new SystemStatusPacket("foo", SystemStatus.DISABLED, tstamp);
        assertEquals("foo", packet.getClientID());
        assertEquals(SystemStatus.DISABLED, packet.getStatus());
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SystemStatusPacket packet = new SystemStatusPacket("foo", SystemStatus.DISABLED, tstamp);

        String expected = "{\"client_id\":\"foo\",\"status\":" + SystemStatus.DISABLED.getValue() +
                ",\"timestamp\":\"" + tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"client_id\":\"foo\",\"status\":" + SystemStatus.DISABLED.getValue() +
                ",\"timestamp\":\"" + tstamp.toString() + "\"}";
        try {
            SystemStatusPacket packet = SystemStatusPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals("foo", packet.getClientID());
            assertEquals(SystemStatus.DISABLED, packet.getStatus());
            assertEquals(tstamp, packet.getTimestamp());
        }
        catch (ThingParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPacketBuilder() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SystemStatusPacket packet = new SystemStatusPacket.Builder()
                .setClientID("foo")
                .setStatus(SystemStatus.DISABLED)
                .setTimestamp(tstamp)
                .build();
        assertEquals("foo", packet.getClientID());
        assertEquals(SystemStatus.DISABLED, packet.getStatus());
        assertEquals(tstamp, packet.getTimestamp());
    }
}