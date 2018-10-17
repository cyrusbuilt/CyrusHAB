package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible thermostat states.
 */
public enum ThermostatState implements Valueable<ThermostatState, Integer> {
    /**
     * The system is off.
     */
    OFF(0),

    /**
     * The system is in heat mode.
     */
    HEATING(1),

    /**
     * The system is in cool mode.
     */
    COOLING(2),

    /**
     * The system is running the fan only.
     */
    FAN_ONLY(3),

    /**
     * The system state is unknown.
     */
    UNKNOWN(4),

    /**
     * The system is delaying start because of a recent shutdown.
     */
    DELAY_START(5);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The parameter value.
     */
    ThermostatState(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public ThermostatState getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (ThermostatState s : values()) {
            if (s.value == value) {
                return s;
            }
        }

        return UNKNOWN;
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
