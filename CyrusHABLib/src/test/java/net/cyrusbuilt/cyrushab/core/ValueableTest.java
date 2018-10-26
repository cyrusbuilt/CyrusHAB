package net.cyrusbuilt.cyrushab.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValueableTest {
    private enum FakeValueable implements Valueable<FakeValueable, Integer> {
        FOO(0),
        BAR(1);

        private int value;

        FakeValueable(int value) {
            this.value = value;
        }

        @Override
        public FakeValueable getType(Integer value) {
            if (value == null) {
                return FOO;
            }

            for (FakeValueable fv : values()) {
                if (fv.value == value) {
                    return fv;
                }
            }

            return FOO;
        }

        @Override
        public Integer getValue() {
            return this.value;
        }
    }

    @Test
    public void getTypeTest() {
        FakeValueable fv = FakeValueable.FOO.getType(1);
        assertEquals(FakeValueable.BAR, fv);
    }

    @Test
    public void getValueTest() {
        int expected = 0;
        int actual = FakeValueable.FOO.getValue();
        assertEquals(expected, actual);
    }
}