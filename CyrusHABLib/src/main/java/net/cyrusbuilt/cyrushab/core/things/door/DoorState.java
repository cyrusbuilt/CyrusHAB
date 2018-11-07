package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible door states.
 */
public enum DoorState implements Valueable<DoorState, Integer> {
    /**
     * The door is open.
     */
    OPEN(0),

    /**
     * The door is closed.
     */
    CLOSED(1),

    /**
     * The door state is unknown.
     */
    UNKNOWN(2);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The value to set.
     */
    DoorState(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public DoorState getType(Integer value) {
        if (value == null) {
            return OPEN;
        }

        for (DoorState ds : values()) {
            if (ds.value == value) {
                return ds;
            }
        }

        return OPEN;
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
