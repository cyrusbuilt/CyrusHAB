package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.Disposable;
import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstraction of a thermostat for HVAC.
 */
public abstract class Thermostat implements Thing {
    /**
     * The key name for thermostat mode.
     */
    public static final String THERMOSTAT_MODE = "mode";

    /**
     * A thermostat state change event.
     */
    public static class ThermostatEvent {
        private int _id = -1;
        private ThermostatState _oldState = ThermostatState.UNKNOWN;
        private ThermostatState _newState = ThermostatState.UNKNOWN;
        private ThermostatMode _mode = ThermostatMode.OFF;

        /**
         * Constructs a new instance of {@link ThermostatEvent} with the id, previous state, current state, and mode.
         * @param id The Thing ID.
         * @param oldState The previous state.
         * @param newState The new (current) state.
         * @param mode The thermostat mode.
         */
        public ThermostatEvent(int id, ThermostatState oldState, ThermostatState newState, ThermostatMode mode) {
            _id = id;
            _oldState = oldState;
            _newState = newState;
            _mode = mode;
        }

        /**
         * Gets the Thing ID.
         * @return The ID.
         */
        public int getID() {
            return _id;
        }

        /**
         * Gets the old (previous) state of the thermostat.
         * @return the previous state.
         */
        public ThermostatState getOldState() {
            return _oldState;
        }

        /**
         * Gets the new (current) state of the thermostat.
         * @return the current state.
         */
        public ThermostatState getNewState() {
            return _newState;
        }

        /**
         * Gets the current thermostat mode.
         * @return The operating mode.
         */
        public ThermostatMode getMode() {
            return _mode;
        }
    }

    /**
     * The event listener interface for thermostat state change events.
     */
    public interface OnThermostatStateChangeListener {
        /**
         * Handles state change events.
         * @param event The event info.
         */
        void onStateChanged(ThermostatEvent event);
    }

    private String _name = StringUtils.EMPTY;
    private Object _tag = null;
    private final ThingType _type = ThingType.THERMOSTAT;
    private boolean _isDisposed = false;
    private volatile ThermostatState _state = ThermostatState.UNKNOWN;
    private List<OnThermostatStateChangeListener> _listeners;
    private boolean _isReadonly = true;
    private boolean _enabled = true;
    private int _id = -1;
    private ThermostatMode _mode = ThermostatMode.OFF;

    /**
     * Creates a new instance of {@link Thermostat} (default constructor).
     */
    protected Thermostat() {
        _listeners = new ArrayList<>();
    }

    /**
     * Creates a new instance of {@link Thermostat} with the name of the device.
     * @param name The thermostat name.
     */
    protected Thermostat(String name) {
        _listeners = new ArrayList<>();
        _name = name;
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
    public void setName(final String name) {
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
    public void setTag(final Object tag) {
        _tag = tag;
    }

    /**
     * (non-Javadoc)
     * @see Thing#type()
     */
    @Override
    public ThingType type() {
        return ThingType.THERMOSTAT;
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
     * Sets whether or not this device is read-only. A read-only device can get status but can't be controlled.
     * @param readonly Set true if read-only.
     */
    protected void setIsReadonly(boolean readonly) {
        _isReadonly = readonly;
    }

    /**
     * (non-Javadoc)
     * @see Thing#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * Sets whether this device is enabled.
     * @param enabled Set true to enable.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    protected void setEnabled(boolean enabled) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Thermostat.class.getCanonicalName());
        }
        _enabled = enabled;
    }

    /**
     * Adds an event listener for this Switch instance.
     * @param listener The listener to add.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void addListener(OnThermostatStateChangeListener listener) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(this.getClass().getCanonicalName());
        }

        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

    /**
     * Notify all registered listeners of a state change event.
     * @param event The event info.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void notifyListeners(ThermostatEvent event) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(this.getClass().getCanonicalName());
        }

        for (OnThermostatStateChangeListener listener : _listeners) {
            listener.onStateChanged(event);
        }
    }

    /**
     * (non-Javadoc)
     * @see Disposable#dispose()
     */
    public void dispose() {
        if (_isDisposed) {
            return;
        }

        if (_listeners != null) {
            _listeners.clear();
            _listeners = null;
        }

        _name = null;
        _tag = null;
        _state = ThermostatState.UNKNOWN;
        _mode = ThermostatMode.OFF;
        _enabled = false;
        _isReadonly = false;
        _isDisposed = true;
    }

    /**
     * Gets the current state of the switch.
     * @return The current state.
     */
    public synchronized ThermostatState state() {
        return _state;
    }

    /**
     * Sets the current state of the thermostat.
     * @param state The state to set.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    protected synchronized void setState(ThermostatState state) throws ObjectDisposedException {
        if (_state != state) {
            notifyListeners(new ThermostatEvent(_id, _state, state, _mode));
            _state = state;
        }
    }

    /**
     * Gets the current mode.
     * @return The current operating mode.
     */
    public ThermostatMode mode() {
        return _mode;
    }

    /**
     * Sets the operating mode.
     * @param mode The operating mode.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void setMode(ThermostatMode mode) throws ObjectDisposedException {
        if (_mode != mode) {
            notifyListeners(new ThermostatEvent(_id, _state, _state, mode));
            _mode = mode;
        }
    }

    /**
     * Maps pertinent state values from the specified status packet.
     * @param packet The status packet.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void mapFromStatusPacket(@NotNull ThermostatStatusPacket packet) throws ObjectDisposedException {
        setEnabled(packet.isEnabled());
        setIsReadonly(packet.isReadonly());
        setMode(packet.getMode());
        setState(packet.getState());
    }
}
