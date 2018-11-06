package net.cyrusbuilt.cyrushab.core.things;

import net.cyrusbuilt.cyrushab.core.Disposable;
import org.jetbrains.annotations.Nullable;

/**
 * A "thing" which is an abstraction of a device that can be sampled/controlled with this framework.
 */
public interface Thing extends Disposable {
    // NOTE: I am aware that static constants in an interface that is to be implemented is "bad practice".
    // However, since these constants should be available to all "Things" for the purpose of being able to reference
    // a uniform set of keys for JSON objects, I did it anyway.  I may change this in the future by deprecating them
    // and moving them to a separate class or something. But for now, it just makes sense. Since I primarily built this
    // project for my own personal use, I don't really care.

    /**
     * The key name for the Thing ID field.
     */
    String THING_ID = "id";

    /**
     * The key name for the client ID field.
     */
    String THING_CLIENT_ID = "client_id";

    /**
     * The key name for the name field.
     */
    String THING_NAME = "name";

    /**
     * The key name for the state field.
     */
    String THING_STATE = "state";

    /**
     * The key name for the type field.
     */
    String THING_TYPE = "type";

    /**
     * The key name for the enabled flag field.
     */
    String THING_ENABLED = "enabled";

    /**
     * The key name for the read-only flag field.
     */
    String THING_READONLY = "readonly";

    /**
     * The key name for the timestamp field.
     */
    String THING_TIMESTAMP = "timestamp";

    /**
     * Gets the name of the device.
     * @return The name of the device.
     */
    String name();

    /**
     * Sets the name of the device.
     * @param name The name of the device.
     */
    void setName(String name);

    /**
     * Get the thing ID.
     * @return The thing ID.
     */
    int id();

    /**
     * Set the thing ID.
     * @param id The thing ID.
     */
    void setThingID(int id);

    /**
     * Get the client ID.
     * @return The client ID.
     */
    String clientID();

    /**
     * Set the client ID.
     * @param clientID The client ID.
     */
    void setClientID(String clientID);

    /**
     * Gets the object this instance is tagged with.
     * @return The object instance this instance is tagged with.
     */
    @Nullable
    Object tag();

    /**
     * Sets the tag for this instance (any object instance). Useful for linking this object with another.
     * @param tag The object tag.
     */
    void setTag(Object tag);

    /**
     * Gets the type of thing.
     * @return The thing type.
     */
    ThingType type();

    /**
     * Gets whether this device is readonly (not controllable, but can get status).
     * @return true if read-only; Otherwise, false.
     */
    boolean isReadonly();

    /**
     * Gets whether this device is enabled.
     * @return true if enabled; Otherwise, false.
     */
    boolean isEnabled();
}
