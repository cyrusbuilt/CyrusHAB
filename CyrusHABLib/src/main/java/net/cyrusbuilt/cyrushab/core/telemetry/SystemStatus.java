package net.cyrusbuilt.cyrushab.core.telemetry;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible system states.
 */
public enum SystemStatus implements Valueable<SystemStatus, Integer> {
    /**
     * The system is operating normally.
     */
    NORMAL(0),

    /**
     * The system is disabled.
     */
    DISABLED(1),

    /**
     * The system status is unknown.
     */
    UNKNOWN(2),

    /**
     * The system is reconnecting to the MQTT broker.
     */
    RECONNECTING(3),

    /**
     * The system is disconnected from the MQTT broker.
     */
    DISCONNECTED(4),

    /**
     * The system is shutting down.
     */
    SHUTDOWN(5);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The parameter value.
     */
    SystemStatus(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public SystemStatus getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (SystemStatus s : values()) {
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
