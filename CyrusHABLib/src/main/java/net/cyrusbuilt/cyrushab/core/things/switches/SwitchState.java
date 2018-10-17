package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible switch states.
 */
public enum SwitchState implements Valueable<SwitchState, Integer> {
    /**
     * The switch is in the "Off" state.
     */
    OFF(0),

    /**
     * The switch is in the "On" state.
     */
    ON(1);

    private int value;

    /**
     * Sets the value of parameter.
     * @param value The parameter value.
     */
    SwitchState(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public SwitchState getType(Integer value) {
        if (value == null) {
            return OFF;
        }

        for (SwitchState s : values()) {
            if (s.value == value) {
                return s;
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
