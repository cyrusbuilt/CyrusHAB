package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.Valueable;

public enum DoorState implements Valueable<DoorState, Integer> {
    OPEN(0),
    CLOSED(1),
    UNKNOWN(2);

    private int value;

    DoorState(int value) {
        this.value = value;
    }

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

    @Override
    public Integer getValue() {
        return this.value;
    }
}
