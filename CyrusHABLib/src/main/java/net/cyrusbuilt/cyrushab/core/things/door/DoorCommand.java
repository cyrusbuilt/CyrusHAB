package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.Valueable;

public enum DoorCommand implements Valueable<DoorCommand, Integer> {
    OPEN(0),
    CLOSE(1),
    UNKNOWN(2);

    private int value;

    DoorCommand(int value) {
        this.value = value;
    }

    @Override
    public DoorCommand getType(Integer value) {
        if (value == null) {
            return UNKNOWN;
        }

        for (DoorCommand dc : values()) {
            if (dc.value == value) {
                return UNKNOWN;
            }
        }

        return UNKNOWN;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
