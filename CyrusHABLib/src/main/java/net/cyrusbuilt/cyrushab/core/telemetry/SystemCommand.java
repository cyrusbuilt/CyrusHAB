package net.cyrusbuilt.cyrushab.core.telemetry;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible system control commands.
 */
public enum SystemCommand implements Valueable<SystemCommand, Integer> {
    /**
     * Shutdown the daemon.
     */
    SHUTDOWN(0),

    /**
     * Disable event processing.
     */
    DISABLE(1),

    /**
     * Enable event processing.
     */
    ENABLE(2),

    /**
     * Restart the daemon.
     */
    RESTART(3),

    /**
     * Request system status.
     */
    GET_SYS_STATUS(4),

    /**
     * Request the status of all devices.
     */
    GET_ALL_DEVICE_STATUS(5),

    /**
     * Request heartbeat (acknowledgement).
     */
    HEARTBEAT(6),

    /**
     * Request inventory of all registered Things.
     */
    GET_ALL_THE_THINGS(7),

    /**
     * Command is unknown.
     */
    UNKNOWN(4);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The value to set.
     */
    SystemCommand(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public SystemCommand getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (SystemCommand cmd : values()) {
            if (cmd.value == value) {
                return cmd;
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
