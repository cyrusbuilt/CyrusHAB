package net.cyrusbuilt.cyrushab.core.things.motionsensor;

import net.cyrusbuilt.cyrushab.core.things.ThingParseException;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class MotionSensorStatusPacketTest {
    @Test
    public void setThingID() {
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket();
        packet.setThingID(1);
        assertEquals(1, packet.getThingID());
    }

    @Test
    public void setClientID() {
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void setTimestamp() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket();
        packet.setTimestamp(tstamp);
        assertEquals(tstamp, packet.getTimestamp());
    }

    @Test
    public void setEnabled() {
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void setState() {
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket();
        packet.setState(MotionSensorState.TRIPPED);
        assertEquals(MotionSensorState.TRIPPED, packet.getState());
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket();
        packet.setState(MotionSensorState.TRIPPED);
        packet.setEnabled(true);
        packet.setTimestamp(tstamp);
        packet.setClientID("foo");
        packet.setThingID(1);

        String expected = "{\"readonly\":true,\"id\":1,\"state\":" + MotionSensorState.TRIPPED.getValue() + ",\"type\":" +
                ThingType.MOTION_SENSOR.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":\"" +
                tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"readonly\":true,\"id\":1,\"state\":" + MotionSensorState.TRIPPED.getValue() + ",\"type\":" +
                ThingType.MOTION_SENSOR.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":\"" +
                tstamp.toString() + "\"}";
        try {
            MotionSensorStatusPacket packet = MotionSensorStatusPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals("foo", packet.getClientID());
            assertEquals(1, packet.getThingID());
            assertEquals(MotionSensorState.TRIPPED, packet.getState());
            assertEquals(ThingType.MOTION_SENSOR, packet.getType());
            assertEquals(tstamp, packet.getTimestamp());
            assertTrue(packet.isEnabled());
            assertTrue(packet.isReadonly());
        }
        catch (ThingParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPacketBuilder() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        MotionSensorStatusPacket packet = new MotionSensorStatusPacket.Builder()
                .setThingID(1)
                .setClientID("foo")
                .setState(MotionSensorState.TRIPPED)
                .setTimestamp(tstamp)
                .setEnabled(true)
                .build();
        assertEquals(1, packet.getThingID());
        assertEquals("foo", packet.getClientID());
        assertEquals(MotionSensorState.TRIPPED, packet.getState());
        assertEquals(tstamp, packet.getTimestamp());
        assertTrue(packet.isEnabled());
    }
}