package net.cyrusbuilt.cyrushab.core.application;

import net.cyrusbuilt.cyrushab.core.telemetry.SystemStatus;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class HeartBeatPacketTest {
    @Test
    public void setClientID() {
        HeartBeatPacket packet = new HeartBeatPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void setHostID() {
        HeartBeatPacket packet = new HeartBeatPacket();
        packet.setHostID("bar");
        assertEquals("bar", packet.getHostID());
    }

    @Test
    public void setSystemStatus() {
        HeartBeatPacket packet = new HeartBeatPacket();
        packet.setSystemStatus(SystemStatus.DISABLED);
        assertEquals(SystemStatus.DISABLED, packet.getStatus());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        HeartBeatPacket packet = new HeartBeatPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        HeartBeatPacket packet = new HeartBeatPacket();
        packet.setClientID("foo");
        packet.setHostID("bar");
        packet.setSystemStatus(SystemStatus.DISABLED);
        packet.setTimestamp(tstamp);

        String expected = "{\"host_client_id\":\"bar\",\"client_id\":\"foo\",\"timestamp\":\"" + tstamp.toString() +
                "\",\"status\":" + SystemStatus.DISABLED.getValue() + "}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void testPacketBuilder() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        HeartBeatPacket packet = new HeartBeatPacket.Builder()
                .setClientID("foo")
                .setHostID("bar")
                .setStatus(SystemStatus.DISABLED)
                .setTimestamp(tstamp)
                .build();
        assertEquals("foo", packet.getClientID());
        assertEquals("bar", packet.getHostID());
        assertEquals(SystemStatus.DISABLED, packet.getStatus());
        assertEquals(tstamp, packet.getTimestamp());
    }
}