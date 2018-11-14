package net.cyrusbuilt.cyrushab.core.telemetry;

import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class SystemControlPacketTest {
    @Test
    public void testCtor() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SystemControlPacket packet = new SystemControlPacket(SystemCommand.DISABLE, "foo", tstamp);
        assertEquals(SystemCommand.DISABLE, packet.getCommand());
        assertEquals("foo", packet.getClientID());
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SystemControlPacket packet = new SystemControlPacket(SystemCommand.DISABLE, "foo", tstamp);

        String expected = "{\"type\":" + ThingType.SYSTEM.getValue() + ",\"client_id\":\"foo\",\"command\":" +
                SystemCommand.DISABLE.getValue() + ",\"timestamp\":\"" + tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"type\":" + ThingType.SYSTEM.getValue() + ",\"client_id\":\"foo\",\"command\":" +
                SystemCommand.DISABLE.getValue() + ",\"timestamp\":\"" + tstamp.toString() + "\"}";
        try {
            SystemControlPacket packet = SystemControlPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(SystemCommand.DISABLE, packet.getCommand());
            assertEquals("foo", packet.getClientID());
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
        SystemControlPacket packet = new SystemControlPacket.Builder()
                .setClientID("foo")
                .setCommand(SystemCommand.DISABLE)
                .setTimestamp(tstamp)
                .build();
        assertEquals(SystemCommand.DISABLE, packet.getCommand());
        assertEquals("foo", packet.getClientID());
        assertEquals(tstamp, packet.getTimestamp());
    }
}