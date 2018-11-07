package net.cyrusbuilt.cyrushab.core.things;

import net.cyrusbuilt.cyrushab.core.Valueable;

/**
 * Possible Thing types.
 */
public enum ThingType implements Valueable<ThingType, Integer> {
    /**
     * The Thing is a thermostat.
     */
    THERMOSTAT(1),

    /**
     * The Thing is a switch or relay.
     */
    SWITCH(2),

    /**
     * The Thing is a dimmable light.
     */
    DIMMABLE_LIGHT(3),

    /**
     * The Thing is a motion sensor.
     */
    MOTION_SENSOR(4),

    /**
     * The Thing is a door.
     */
    DOOR(5),

    /**
     * The thing in question is actually the controller system itself.
     */
    SYSTEM(6),

    /**
     * The thing in question is actually the client app.
     */
    APP(7),

    /**
     * The Thing type is unknown.
     */
    UNKNOWN(0);

    private int value;

    /**
     * Set the value of the parameter.
     * @param value The parameter value.
     */
    ThingType(int value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * @see Valueable#getType(Object)
     */
    @Override
    public ThingType getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (ThingType t :  values()) {
            if (t.value == value) {
                return t;
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
        return value;
    }
}
