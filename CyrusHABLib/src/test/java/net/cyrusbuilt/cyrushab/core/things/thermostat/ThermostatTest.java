package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThermostatTest {
    @Test
    public void setThingID() {
        Thermostat tstat = new Thermostat() {
            @Override
            public int id() {
                return super.id();
            }

            @Override
            public void setThingID(int id) {
                super.setThingID(id);
            }
        };

        tstat.setThingID(1);
        assertEquals(1, tstat.id());
    }

    @Test
    public void setClientID() {
        Thermostat tstat = new Thermostat() {
            @Override
            public String clientID() {
                return super.clientID();
            }

            @Override
            public void setClientID(String clientID) {
                super.setClientID(clientID);
            }
        };

        tstat.setClientID("foo");
        assertEquals("foo", tstat.clientID());
    }

    @Test
    public void setName() {
        Thermostat tstat = new Thermostat() {
            @Override
            public String name() {
                return super.name();
            }

            @Override
            public void setName(String name) {
                super.setName(name);
            }
        };

        tstat.setName("bar");
        assertEquals("bar", tstat.name());
    }

    @Test
    public void setTag() {
        Thermostat tstat = new Thermostat() {
            @Override
            public Object tag() {
                return super.tag();
            }

            @Override
            public void setTag(Object tag) {
                super.setTag(tag);
            }
        };

        Object obj = new Object();
        tstat.setTag(obj);
        assertEquals(obj, tstat.tag());
    }

    @Test
    public void isDisposed() {
        Thermostat tstat = new Thermostat() {
            @Override
            public boolean isDisposed() {
                return super.isDisposed();
            }

            @Override
            public void dispose() {
                super.dispose();
            }
        };

        assertFalse(tstat.isDisposed());
        tstat.dispose();
        assertTrue(tstat.isDisposed());
    }

    @Test
    public void isReadonly() {
        Thermostat tstat = new Thermostat() {
            @Override
            public boolean isReadonly() {
                return super.isReadonly();
            }

            @Override
            public void setIsReadonly(boolean readonly) {
                super.setIsReadonly(readonly);
            }
        };

        assertTrue(tstat.isReadonly());
        tstat.setIsReadonly(false);
        assertFalse(tstat.isReadonly());
    }

    @Test
    public void isEnabled() {
        Thermostat tstat = new Thermostat() {
            @Override
            public boolean isEnabled() {
                return super.isEnabled();
            }

            @Override
            public void setEnabled(boolean enabled) throws ObjectDisposedException {
                super.setEnabled(enabled);
            }
        };

        assertTrue(tstat.isEnabled());

        try {
            tstat.setEnabled(false);
            assertFalse(tstat.isEnabled());
        }
        catch (ObjectDisposedException ignored) {
            fail();
        }
    }

    @Test
    public void addListener() {
        Thermostat tstat = new Thermostat() {
            @Override
            public void addListener(OnThermostatStateChangeListener listener) throws ObjectDisposedException {
                super.addListener(listener);
            }

            @Override
            public void notifyListeners(ThermostatEvent event) throws ObjectDisposedException {
                super.notifyListeners(event);
            }
        };

        try {
            tstat.addListener(event -> {
                assertEquals(1, event.getID());
                assertEquals(ThermostatState.COOLING, event.getOldState());
                assertEquals(ThermostatState.OFF, event.getNewState());
                assertEquals(ThermostatMode.OFF, event.getMode());
            });

            tstat.notifyListeners(new Thermostat.ThermostatEvent(1, ThermostatState.COOLING, ThermostatState.OFF, ThermostatMode.OFF));
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }


    @Test
    public void state() {
        Thermostat tstat = new Thermostat() {
            @Override
            public synchronized ThermostatState state() {
                return super.state();
            }

            @Override
            protected synchronized void setState(ThermostatState state) throws ObjectDisposedException {
                super.setState(state);
            }
        };

        try {
            tstat.setState(ThermostatState.COOLING);
            assertEquals(ThermostatState.COOLING, tstat.state());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void mode() {
        Thermostat tstat = new Thermostat() {
            @Override
            public ThermostatMode mode() {
                return super.mode();
            }

            @Override
            public void setMode(ThermostatMode mode) throws ObjectDisposedException {
                super.setMode(mode);
            }
        };

        try {
            tstat.setMode(ThermostatMode.COOL);
            assertEquals(ThermostatMode.COOL, tstat.mode());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

    @Test
    public void mapFromStatusPacket() {
        Thermostat tstat = new Thermostat() {};
        try {
            tstat.mapFromStatusPacket(new ThermostatStatusPacket.Builder()
                    .setMode(ThermostatMode.HEAT)
                    .setState(ThermostatState.COOLING)
                    .setReadonly(false)
                    .setEnabled(true)
                    .build()
            );

            assertEquals(ThermostatMode.HEAT, tstat.mode());
            assertEquals(ThermostatState.COOLING, tstat.state());
            assertFalse(tstat.isReadonly());
            assertTrue(tstat.isEnabled());
        }
        catch (ObjectDisposedException e) {
            fail();
        }
    }

}