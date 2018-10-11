package net.cyrusbuilt.cyrushab.core;

import org.apache.commons.lang3.StringUtils;

/**
 * The exception that is thrown when attempting to operate on an object instance that has been disposed.
 */
public class ObjectDisposedException extends Exception {
    /**
     * Creates a new instance of ObjectDisposedException with the name of the disposed object.
     * @param className The class name of the disposed object.
     */
    public ObjectDisposedException(String className) {
        super((StringUtils.isBlank(className) ? "Object" : className) +
                " instance has been disposed and cannot be used.");
    }
}
