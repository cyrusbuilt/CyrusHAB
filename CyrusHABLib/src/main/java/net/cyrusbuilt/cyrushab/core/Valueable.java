package net.cyrusbuilt.cyrushab.core;

/**
 * This interface is intended for {@code enums} (or similar classes that needs
 * to be identified by a value) who are based on a value for each constant,
 * where it has the utility methods to identify the type ({@code enum} constant)
 * based on the value passed, and can declare it's value in the interface as
 * well
 *
 * @param <T>
 *            the type of the constants (pass the {@code enum} as a type)
 * @param <V>
 *            the type of the value which identifies this constant
 */
public interface Valueable<T extends Valueable<T, V>, V> {

    /**
     * get the Type based on the passed value
     *
     * @param value
     *            the value that identifies the Type
     * @return the Type
     */
    T getType(V value);

    /**
     * get the value that identifies this type
     *
     * @return a value that can be used later in {@link #getType(Object)}
     */
    V getValue();
}