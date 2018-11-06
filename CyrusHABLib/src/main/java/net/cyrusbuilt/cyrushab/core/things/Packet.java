package net.cyrusbuilt.cyrushab.core.things;

import java.sql.Timestamp;

/**
 * Represents packet data.
 */
public interface Packet {
    /**
     * Get the client ID.
     * @return The client ID.
     */
    String getClientID();

    /**
     * Sets the client ID.
     * @param clientID The client ID.
     */
    void setClientID(String clientID);

    /**
     * Gets the timestamp.
     * @return The timestamp.
     */
    Timestamp getTimestamp();

    /**
     * Sets the timestamp.
     * @param timestamp The timestamp.
     */
    void setTimestamp(Timestamp timestamp);

    /**
     * Converts the packet data to a JSON string representation.
     * @return A string representation of the JSON object.
     */
    String toJsonString();

    /**
     * A builder class for the specified packet type.
     * @param <T> The packet type.
     */
    interface Builder<T> {
        /**
         * Sets the timestamp.
         * @param timestamp The timestamp.
         */
        Builder<T> setTimestamp(Timestamp timestamp);

        /**
         * Combines all the options and returns a packet object for the specified type.
         * @return The constructed packet.
         */
        T build();
    }
}
