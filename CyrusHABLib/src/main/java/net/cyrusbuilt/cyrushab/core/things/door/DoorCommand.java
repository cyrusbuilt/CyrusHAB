package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible door commands.
 */
public enum DoorCommand implements Valueable<DoorCommand, Integer> {
    /**
     * Open the door.
     */
    OPEN(0),

    /**
     * Close the door.
     */
    CLOSE(1),

    /**
     * The door command is unknown.
     */
    UNKNOWN(2);

    private int value;

    /**
     * Sets the parameter value.
     * @param value The value to set.
     */
    DoorCommand(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public DoorCommand getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (DoorCommand dc : values()) {
            if (dc.value == value) {
                return dc;
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
