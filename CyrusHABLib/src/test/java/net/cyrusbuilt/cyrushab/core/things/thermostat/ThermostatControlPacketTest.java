package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class ThermostatControlPacketTest {
    @Test
    public void getID() {
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setID(1);
        assertEquals(1, packet.getID());
    }

    @Test
    public void getClientID() {
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setClientID("foo");
        assertEquals("foo", packet.getClientID());
    }

    @Test
    public void getMode() {
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setMode(ThermostatMode.HEAT);
        assertEquals(ThermostatMode.HEAT, packet.getMode());
    }

    @Test
    public void isEnabled() {
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void isReadonly() {
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void getTimestamp() {
        Timestamp expected = new Timestamp(System.currentTimeMillis());
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setTimestamp(expected);
        Timestamp actual = packet.getTimestamp();
        assertEquals(expected, actual);
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        ThermostatControlPacket packet = new ThermostatControlPacket();
        packet.setID(1);
        packet.setClientID("foo");
        packet.setReadonly(false);
        packet.setEnabled(true);
        packet.setMode(ThermostatMode.HEAT);
        packet.setTimestamp(tstamp);

        String expected = "{\"mode\":" + ThermostatMode.HEAT.getValue() + ",\"readonly\":false,\"id\":1,\"type\":" +
                ThingType.THERMOSTAT.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":\"" +
                tstamp.toString() + "\"}";
        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"mode\":" + ThermostatMode.HEAT.getValue() + ",\"readonly\":false,\"id\":1,\"type\":" +
                ThingType.THERMOSTAT.getValue() + ",\"client_id\":\"foo\",\"enabled\":true,\"timestamp\":\"" +
                tstamp.toString() + "\"}";

        try {
            ThermostatControlPacket packet = ThermostatControlPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals("foo", packet.getClientID());
            assertEquals(1, packet.getID());
            assertEquals(ThermostatMode.HEAT, packet.getMode());
            assertEquals(tstamp, packet.getTimestamp());
            assertTrue(packet.isEnabled());
            assertFalse(packet.isReadonly());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPacketBuilder() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        ThermostatControlPacket packet = new ThermostatControlPacket.Builder()
                .setClientID("foo")
                .setThingID(1)
                .setMode(ThermostatMode.COOL)
                .setEnabled(true)
                .setReadonly(false)
                .setTimestamp(tstamp)
                .build();

        assertEquals("foo", packet.getClientID());
        assertEquals(1, packet.getID());
        assertEquals(ThermostatMode.COOL, packet.getMode());
        assertEquals(tstamp, packet.getTimestamp());
        assertTrue(packet.isEnabled());
        assertFalse(packet.isReadonly());
    }
}