package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.Valueable;

public enum ThermostatMode implements Valueable<ThermostatMode, Integer> {
    OFF(0),
    HEAT(1),
    COOL(2),
    FAN_ONLY(3);

    private int value;

    ThermostatMode(Integer value) {
        this.value = value;
    }

    @Override
    public ThermostatMode getType(Integer value) {
        if (value == null) {
            return OFF;
        }

        for (ThermostatMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }

        return OFF;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
