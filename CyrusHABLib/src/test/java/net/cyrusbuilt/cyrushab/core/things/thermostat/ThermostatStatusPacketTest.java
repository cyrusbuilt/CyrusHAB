package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.*;

public class ThermostatStatusPacketTest {

    @Test
    public void getID() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setID(1);

        int expected = 1;
        int actual = packet.getID();
        assertEquals(expected, actual);
    }

    @Test
    public void getName() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setName("foo");
        assertEquals("foo", packet.getName());
    }

    @Test
    public void getClientID() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setClientID("bar");
        assertEquals("bar", packet.getClientID());
    }

    @Test
    public void getState() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setState(ThermostatState.COOLING);
        assertEquals(ThermostatState.COOLING, packet.getState());
    }

    @Test
    public void getMode() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setMode(ThermostatMode.HEAT);
        assertEquals(ThermostatMode.HEAT, packet.getMode());
    }

    @Test
    public void isEnabled() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setEnabled(true);
        assertTrue(packet.isEnabled());
    }

    @Test
    public void isReadonly() {
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setReadonly(true);
        assertTrue(packet.isReadonly());
    }

    @Test
    public void getTimestamp() {
        Timestamp expected = new Timestamp(System.currentTimeMillis());

        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setTimestamp(expected);

        Timestamp actual = packet.getTimestamp();
        assertEquals(expected, actual);
    }

    @Test
    public void toJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        ThermostatStatusPacket packet = new ThermostatStatusPacket();
        packet.setClientID("foo");
        packet.setName("bar");
        packet.setID(1);
        packet.setState(ThermostatState.COOLING);
        packet.setMode(ThermostatMode.COOL);
        packet.setEnabled(true);
        packet.setReadonly(false);
        packet.setTimestamp(tstamp);

        String expected = "{\"mode\":" + ThermostatMode.COOL.getValue() + ",\"readonly\":false,\"name\":\"bar\"," +
                            "\"id\":1,\"state\":" + ThermostatState.COOLING.getValue() + ",\"type\":" +
                            ThingType.THERMOSTAT.getValue() + ",\"client_id\":\"foo\",\"enabled\":true," +
                            "\"timestamp\":\"" + tstamp.toString() + "\"}";

        String actual = packet.toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void fromJsonString() {
        Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        String test = "{\"mode\":" + ThermostatMode.COOL.getValue() + ",\"readonly\":false,\"name\":\"bar\"," +
                "\"id\":1,\"state\":" + ThermostatState.COOLING.getValue() + ",\"type\":" +
                ThingType.THERMOSTAT.getValue() + ",\"client_id\":\"foo\",\"enabled\":true," +
                "\"timestamp\":\"" + tstamp.toString() + "\"}";

        try {
            ThermostatStatusPacket packet = ThermostatStatusPacket.fromJsonString(test);
            assertNotNull(packet);
            assertEquals(ThermostatMode.COOL, packet.getMode());
            assertEquals(ThermostatState.COOLING, packet.getState());
            assertEquals("foo", packet.getClientID());
            assertEquals("bar", packet.getName());
            assertEquals(1, packet.getID());
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
        ThermostatStatusPacket packet = new ThermostatStatusPacket.Builder()
                .setClientID("foo")
                .setName("bar")
                .setID(1)
                .setEnabled(true)
                .setReadonly(false)
                .setState(ThermostatState.COOLING)
                .setMode(ThermostatMode.COOL)
                .setTimestamp(tstamp)
                .build();

        assertEquals("foo", packet.getClientID());
        assertEquals("bar", packet.getName());
        assertEquals(1, packet.getID());
        assertEquals(ThermostatState.COOLING, packet.getState());
        assertEquals(ThermostatMode.COOL, packet.getMode());
        assertEquals(tstamp, packet.getTimestamp());
        assertTrue(packet.isEnabled());
        assertFalse(packet.isReadonly());
    }
}