package net.cyrusbuilt.cyrushab.core.things;

import net.cyrusbuilt.cyrushab.core.Valueable;

public enum ThingType implements Valueable<ThingType, Integer> {
    THERMOSTAT(1),
    SWITCH(2),
    DIMMABLE_LIGHT(3),
    MOTION_SENSOR(4),
    UNKNOWN(0);

    private int value;

    ThingType(int value) {
        this.value = value;
    }

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

    @Override
    public Integer getValue() {
        return value;
    }
}
