package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible thermostat modes.
 */
public enum ThermostatMode implements Valueable<ThermostatMode, Integer> {
    /**
     * Turns the thermostat off.
     */
    OFF(0),

    /**
     * Sets the thermostat mode to "Heat".
     */
    HEAT(1),

    /**
     * Sets the thermostat mode to "Cool".
     */
    COOL(2),

    /**
     * Sets the thermostat mode to "Fan only".
     */
    FAN_ONLY(3);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The parameter value.
     */
    ThermostatMode(Integer value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
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

    /**
     * (non-Javadoc)
     * @see Valueable#getValue()
     */
    @Override
    public Integer getValue() {
        return this.value;
    }
}
