package net.cyrusbuilt.cyrushab.core.things.motionsensor;

import net.cyrusbuilt.cyrushab.core.Disposable;
import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstraction of a motion sensor.
 */
public abstract class MotionSensor implements Thing {
    /**
     * A motion sensor state change event info object.
     */
    public static class MotionSensorEvent {
        private MotionSensorState _oldState = MotionSensorState.UNKNOWN;
        private MotionSensorState _newState = MotionSensorState.UNKNOWN;

        /**
         * Constructs a new instance of {@link MotionSensorEvent}.
         * @param oldState The old (previous) state of the sensor.
         * @param newState The new (current) state of the sensor.
         */
        public MotionSensorEvent(MotionSensorState oldState, MotionSensorState newState) {
            _oldState = oldState;
            _newState = newState;
        }

        /**
         * Gets the old (previous) state.
         * @return The previous state.
         */
        public MotionSensorState getOldState() {
            return _oldState;
        }

        /**
         * Gets the new (current) state.
         * @return The current state.
         */
        public MotionSensorState getNewState() {
            return _newState;
        }
    }

    /**
     * The event listener interface for motion sensor state change events.
     */
    public interface OnMotionSensorStateChangedListener {
        /**
         * Handle state change events.
         * @param event The event info.
         */
        void onStateChanged(MotionSensorEvent event);
    }

    private static final Object _lock = new Object();
    private int _id = -1;
    private String _name = StringUtils.EMPTY;
    private String _clientID = StringUtils.EMPTY;
    private Object _tag = null;
    private boolean _isDisposed = false;
    private boolean _isEnabled = false;
    private List<OnMotionSensorStateChangedListener> _listeners;
    private volatile MotionSensorState _state = MotionSensorState.UNKNOWN;

    /**
     * Constructs a new instance of {@link MotionSensor}.
     */
    public MotionSensor() {
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
        return ThingType.MOTION_SENSOR;
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
        return true;
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
     * Gets the current state of the motion sensor.
     * @return The current state.
     */
    public synchronized MotionSensorState getState() {
        return _state;
    }

    /**
     * Gets whether the sensor is tripped.
     * @return true if the sensor is tripped; Otherwise, false.
     */
    public boolean isTripped() {
        return getState() == MotionSensorState.TRIPPED;
    }

    /**
     * Adds a motion sensor state change event listener.
     * @param listener The listener to add.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void addListener(@NotNull OnMotionSensorStateChangedListener listener) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(MotionSensor.class.getSimpleName());
        }

        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    /**
     * Notifies registered listeners that a motion sensor state change occurred.
     * @param event The event info.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void notifyListeners(@NotNull MotionSensorEvent event) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(MotionSensor.class.getSimpleName());
        }

        for (OnMotionSensorStateChangedListener listener : _listeners) {
            listener.onStateChanged(event);
        }
    }

    /**
     * Sets the sensor state.
     * @param state The state to set.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public synchronized void setState(MotionSensorState state) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(MotionSensor.class.getSimpleName());
        }

        if (_state != state) {
            notifyListeners(new MotionSensorEvent(_state, state));
            _state = state;
        }
    }

    /**
     * Sets whether the sensor is enabled.
     * @param enabled Set true to enable.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void setEnabled(boolean enabled) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(MotionSensor.class.getSimpleName());
        }

        _isEnabled = enabled;
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
            _state = MotionSensorState.UNKNOWN;
        }

        _name = null;
        _tag = null;
        _id = -1;
        _isEnabled = false;
        _isDisposed = true;
    }

    /**
     * Maps pertinent values from the specified status packet.
     * @param packet The packet to map from.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void mapFromStatusPacket(MotionSensorStatusPacket packet) throws ObjectDisposedException {
        setEnabled(packet.isEnabled());
        setState(packet.getState());
    }
}
