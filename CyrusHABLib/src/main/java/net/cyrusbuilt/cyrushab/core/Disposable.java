package net.cyrusbuilt.cyrushab.core;

/**
 * A disposable object.
 */
public interface Disposable {
    /**
     * Disposes the objects managed resources.
     */
    void dispose();

    /**
     * Gets whether or not the object instance has been disposed.
     * @return true if disposed; Otherwise, false.
     */
    boolean isDisposed();
}