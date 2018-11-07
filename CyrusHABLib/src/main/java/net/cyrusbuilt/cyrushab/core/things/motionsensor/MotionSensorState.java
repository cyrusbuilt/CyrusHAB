package net.cyrusbuilt.cyrushab.core.things.motionsensor;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible motion sensor states.
 */
public enum MotionSensorState implements Valueable<MotionSensorState, Integer> {
    /**
     * The sensor is idle.
     */
    IDLE(0),

    /**
     * The sensor tripped.
     */
    TRIPPED(1),

    /**
     * The sensor state is unknown.
     */
    UNKNOWN(2);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The value to set.
     */
    MotionSensorState(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public MotionSensorState getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (MotionSensorState state : values()) {
            if (state.value == value) {
                return state;
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
