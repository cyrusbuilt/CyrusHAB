package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class SwitchStatusPacketTest {
    @Test
    public void setID() {
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setID(1);
        assertEquals(1, packet.getID());
    }

    @Test
    public void setName() {
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setName("foo");
        assertEquals("foo", packet.getName());
    }

    @Test
    public void setClientID() {
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setClientID("bar");
        assertEquals("bar", packet.getClientID());
    }

    @Test
    public void setState() {
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setState(SwitchState.ON);
        assertEquals(SwitchState.ON, packet.getState());
    }

    @Test
    public void setEnabled() {
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void setReadonly() {
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SwitchStatusPacket packet = new SwitchStatusPacket();
        packet.setID(1);
        packet.setClientID("foo");
        packet.setName("bar");
        packet.setReadonly(false);
        packet.setEnabled(true);
        packet.setState(SwitchState.ON);
        packet.setTimestamp(tstamp);

        String expected = "{\"readonly\":false,\"name\":\"bar\",\"id\":1,\"state\":" + SwitchState.ON.getValue() +
                ",\"type\":" + ThingType.SWITCH.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":" +
                "\"" + tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"readonly\":false,\"name\":\"bar\",\"id\":1,\"state\":" + SwitchState.ON.getValue() +
                ",\"type\":" + ThingType.SWITCH.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":" +
                "\"" + tstamp.toString() + "\"}";
        try {
            SwitchStatusPacket packet = SwitchStatusPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(1, packet.getID());
            assertEquals("foo", packet.getClientID());
            assertEquals("bar", packet.getName());
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
        SwitchStatusPacket test = new SwitchStatusPacket.Builder()
                .setClientID("foo")
                .setName("bar")
                .setID(1)
                .setEnabled(true)
                .setReadonly(false)
                .setState(SwitchState.ON)
                .setTimestamp(tstamp)
                .build();
        assertEquals("foo", test.getClientID());
        assertEquals("bar", test.getName());
        assertEquals(1, test.getID());
        assertEquals(SwitchState.ON, test.getState());
        assertEquals(tstamp, test.getTimestamp());
        assertTrue(test.isEnabled());
        assertFalse(test.isReadonly());
    }
}