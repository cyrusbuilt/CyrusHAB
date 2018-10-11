package net.cyrusbuilt.cyrushab.core.things;

import net.cyrusbuilt.cyrushab.core.Disposable;
import org.jetbrains.annotations.Nullable;

/**
 * A "thing" which is an abstraction of a device that can be sampled/controlled with this framework.
 */
public interface Thing extends Disposable {
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
     * Gets the MQTT control topic for this device instance.
     * @return The MQTT control topic.
     */
    String getMqttControlTopic();

    /**
     * Sets the MQTT control topic for this device.
     * @param topicName The control topic path/name.
     */
    void setMqttControlTopic(String topicName);

    /**
     * Gets the MQTT status topic for this device.
     * @return The status topic path/name.
     */
    String getMqttStatusTopic();

    /**
     * Sets the MQTT status topic for this device.
     * @param topicName The status topic path/name.
     */
    void setMqttStatusTopic(String topicName);
}
