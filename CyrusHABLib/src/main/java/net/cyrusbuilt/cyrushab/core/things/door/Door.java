package net.cyrusbuilt.cyrushab.core.things.door;

import net.cyrusbuilt.cyrushab.core.Disposable;
import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstraction of a Door.
 */
public abstract class Door implements Thing {
    /**
     * The key name for the lock enable attribute.
     */
    public static final String DOOR_LOCK_ENABLED = "lock_enabled";

    /**
     * The key name for the door command attribute.
     */
    public static final String DOOR_COMMAND = "command";

    /**
     * A door state change event.
     */
    public static class DoorEvent {
        private DoorState _oldState = DoorState.CLOSED;
        private DoorState _newState = DoorState.CLOSED;
        private boolean _lockEnabled = false;

        /**
         * Constructs a new instance of {@link DoorEvent} with the old state, new state, and lock state.
         * @param oldState The old (previous) door state.
         * @param newState The new (current) door state.
         * @param lockEnabled Set true if the door lock is enabled.
         */
        public DoorEvent(DoorState oldState, DoorState newState, boolean lockEnabled) {
            _oldState = oldState;
            _newState = newState;
            _lockEnabled = lockEnabled;
        }

        /**
         * Gets the old (previous) door state.
         * @return The old state.
         */
        public DoorState oldState() {
            return _oldState;
        }

        /**
         * Gets the new (current) door state.
         * @return The current state.
         */
        public DoorState newState() {
            return _newState;
        }

        /**
         * Gets whether the door lock is enabled.
         * @return true if enabled; Otherwise, false.
         */
        public boolean lockEnabled() {
            return _lockEnabled;
        }
    }

    /**
     * The event listener interface for door state change events.
     */
    public interface OnDoorStateChangedListener {
        /**
         * Handles door state change events.
         * @param event The event info.
         */
        void onDoorStateChanged(DoorEvent event);

        /**
         * Handles door lock events.
         * @param event The event info.
         */
        void onDoorLockChanged(DoorEvent event);
    }

    private static final Object _lock = new Object();
    private String _name = StringUtils.EMPTY;
    private String _clientID = StringUtils.EMPTY;
    private Object _tag = null;
    private boolean _isDisposed = false;
    private boolean _isEnabled = false;
    private boolean _isReadonly = false;
    private volatile boolean _lockEnabled = false;
    private int _id = -1;
    private volatile DoorState _state = DoorState.OPEN;
    private List<OnDoorStateChangedListener> _listeners;

    /**
     * Constructs a new instance of {@link Door}.
     */
    public Door() {
        _listeners = new ArrayList<>();
    }

    /**
     * Constructs a new instance of {@link Door} with the name of the door.
     * @param name The name of the door.
     */
    public Door(String name) {
        _name = name;
        _listeners = new ArrayList<>();
    }

    /**
     * (non-Javadoc)
     * @see Thing#id()
     */
    @Override
    public int id() {
        return _id;
    }

    /**
     * (non-Javadoc)
     * @see Thing#setThingID(int)
     */
    @Override
    public void setThingID(int id) {
        _id = id;
    }

    /**
     * (non-Javadoc)
     * @see Thing#clientID()
     */
    @Override
    public String clientID() {
        return _clientID;
    }

    /**
     * (non-Javadoc)
     * @see Thing#setClientID(String)
     */
    @Override
    public void setClientID(String clientID) {
        _clientID = clientID;
    }

    /**
     * (non-Javadoc)
     * @see Thing#name()
     */
    @Override
    public String name() {
        return _name;
    }

    /**
     * (non-Javadoc)
     * @see Thing#setName(String)
     */
    @Override
    public void setName(String name) {
        _name = name;
    }

    /**
     * (non-Javadoc)
     * @see Thing#tag()
     */
    @Override
    public Object tag() {
        return _tag;
    }

    /**
     * (non-Javadoc)
     * @see Thing#setTag(Object)
     */
    @Override
    public void setTag(Object tag) {
        _tag = tag;
    }

    /**
     * (non-Javadoc)
     * @see Thing#type()
     */
    @Override
    public ThingType type() {
        return ThingType.DOOR;
    }

    /**
     * (non-Javadoc)
     * @see Disposable#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    /**
     * (non-Javadoc)
     * @see Thing#isReadonly()
     */
    @Override
    public boolean isReadonly() {
        return _isReadonly;
    }

    /**
     * Sets whether this door is read-only.
     * @param readonly Set true if read-only.
     */
    public void setIsReadonly(boolean readonly) {
        _isReadonly = readonly;
    }

    /**
     * (non-Javadoc)
     * @see Thing#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return _isEnabled;
    }

    /**
     * Gets the current state of the Door.
     * @return The Door state.
     */
    public synchronized DoorState getState() {
        return _state;
    }

    /**
     * Gets whether the door is currently open.
     * @return true if open; Otherwise, false.
     */
    public boolean isOpen() {
        return getState() != DoorState.CLOSED;
    }

    /**
     * Gets whether the door is closed.
     * @return true if closed; Otherwise, false.
     */
    public boolean isClosed() {
        return !isOpen();
    }

    /**
     * Adds a door state change event listener.
     * @param listener The listener to add.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void addListener(OnDoorStateChangedListener listener) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Door.class.getSimpleName());
        }

        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    /**
     * Notifies registered listeners that a door state event occurred.
     * @param event The event info.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void notifyDoorStateListeners(DoorEvent event) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Door.class.getSimpleName());
        }

        for (OnDoorStateChangedListener listener : _listeners) {
            listener.onDoorStateChanged(event);
        }
    }

    /**
     * Notifies registered listeners that a door lock event occurred.
     * @param event The event info.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void notifyDoorLockListeners(DoorEvent event) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Door.class.getSimpleName());
        }

        for (OnDoorStateChangedListener listener : _listeners) {
            listener.onDoorLockChanged(event);
        }
    }

    /**
     * Sets the door state.
     * @param state The door state.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public synchronized void setState(DoorState state) throws ObjectDisposedException {
        if (_state != state) {
            notifyDoorStateListeners(new DoorEvent(_state, state, _lockEnabled));
            _state = state;
        }
    }

    /**
     * Opens the door.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void open() throws ObjectDisposedException {
        setState(DoorState.OPEN);
    }

    /**
     * Closes the door.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void close() throws ObjectDisposedException {
        setState(DoorState.CLOSED);
    }

    /**
     * Sets whether or not the door is enabled.
     * @param enabled Set true to enable.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void setEnabled(boolean enabled) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Door.class.getSimpleName());
        }

        _isEnabled = enabled;
    }

    /**
     * Gets whether or not the door is locked.
     * @return true if locked; Otherwise, false.
     */
    public synchronized boolean isLocked() {
        return _lockEnabled;
    }

    /**
     * Sets whether the door is locked.
     * @param locked Set true to lock.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public synchronized void setLocked(boolean locked) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Door.class.getSimpleName());
        }

        if (_lockEnabled != locked) {
            notifyDoorLockListeners(new DoorEvent(_state, _state, locked));
            _lockEnabled = locked;
        }
    }

    /**
     * (non-Javadoc)
     * @see Disposable#dispose()
     */
    @Override
    public void dispose() {
        if (_isDisposed) {
            return;
        }

        if (_listeners != null) {
            _listeners.clear();
            _listeners = null;
        }

        synchronized (_lock) {
            _state = DoorState.UNKNOWN;
            _lockEnabled = false;
        }

        _name = null;
        _tag = null;
        _id = -1;
        _isEnabled = false;
        _isReadonly = false;
        _isDisposed = true;
    }

    /**
     * Maps pertinent values from the specified status packet.
     * @param packet The packet to map values from.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void mapFromStatusPacket(DoorStatusPacket packet) throws ObjectDisposedException {
        setState(packet.getState());
        setEnabled(packet.isEnabled());
        setIsReadonly(packet.isReadonly());
        setLocked(packet.isLocked());
    }
}
