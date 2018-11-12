package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class SwitchControlPacketTest {
    @Test
    public void setID() {
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setID(1);
        assertEquals(1, packet.getID());
    }

    @Test
    public void setClientID() {
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void setState() {
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setState(SwitchState.ON);
        assertEquals(SwitchState.ON, packet.getState());
    }

    @Test
    public void setEnabled() {
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void setReadonly() {
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SwitchControlPacket packet = new SwitchControlPacket();
        packet.setID(1);
        packet.setClientID("foo");
        packet.setReadonly(false);
        packet.setEnabled(true);
        packet.setState(SwitchState.ON);
        packet.setTimestamp(tstamp);

        String expected = "{\"readonly\":false,\"id\":1,\"state\":" + SwitchState.ON.getValue() + ",\"type\":" +
                ThingType.SWITCH.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":\"" +
                tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"readonly\":false,\"id\":1,\"state\":" + SwitchState.ON.getValue() + ",\"type\":" +
                ThingType.SWITCH.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":\"" +
                tstamp.toString() + "\"}";
        try {
            SwitchControlPacket packet = SwitchControlPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(1, packet.getID());
            assertEquals("foo", packet.getClientID());
            assertEquals(SwitchState.ON, packet.getState());
            assertEquals(tstamp, packet.getTimestamp());
            assertFalse(packet.isReadonly());
            assertTrue(packet.isEnabled());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPacketBuilder() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SwitchControlPacket packet = new SwitchControlPacket.Builder()
                .setClientID("foo")
                .setThingID(1)
                .setTimestamp(tstamp)
                .setEnabled(true)
                .setReadonly(false)
                .setState(SwitchState.ON)
                .build();
        assertEquals("foo", packet.getClientID());
        assertEquals(1, packet.getID());
        assertEquals(tstamp, packet.getTimestamp());
        assertEquals(SwitchState.ON, packet.getState());
        assertFalse(packet.isReadonly());
        assertTrue(packet.isEnabled());
    }
}