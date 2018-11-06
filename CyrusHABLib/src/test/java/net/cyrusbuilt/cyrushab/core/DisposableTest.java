package net.cyrusbuilt.cyrushab.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class DisposableTest {
    private class DummyDisposable implements Disposable {
        private boolean _isDisposed = false;

        public DummyDisposable() {}

        @Override
        public boolean isDisposed() {
            return _isDisposed;
        }

        @Override
        public void dispose() {
            if (_isDisposed) {
                return;
            }

            _isDisposed = true;
        }

        public void throwException() throws ObjectDisposedException {
            if (_isDisposed) {
                throw new ObjectDisposedException(DisposableTest.class.getSimpleName());
            }
        }
    }

    @Test
    public void disposeTest() {
        DummyDisposable obj = new DummyDisposable();
        assertFalse(obj.isDisposed());

        obj.dispose();
        assertTrue(obj.isDisposed());

        boolean thrown = false;
        try {
            obj.throwException();
        }
        catch (ObjectDisposedException e) {
            thrown = true;
        }

        assertTrue(thrown);
    }
}