package net.cyrusbuilt.cyrushab.core.things.thermostat;

import net.cyrusbuilt.cyrushab.core.Disposable;
import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;
import org.apache.commons.lang3.StringUtils;

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
        private String _name = StringUtils.EMPTY;
        private ThermostatState _oldState = ThermostatState.UNKNOWN;
        private ThermostatState _newState = ThermostatState.UNKNOWN;

        /**
         * Constructs a new instance of {@link ThermostatEvent} with the name, previous state, and current state.
         * @param name The name of the thermostat.
         * @param oldState The previous state.
         * @param newState The new (current) state.
         */
        public ThermostatEvent(String name, ThermostatState oldState, ThermostatState newState) {
            _name = name;
            _oldState = oldState;
            _newState = newState;
        }

        /**
         * Gets the name of the thermostat.
         * @return The name.
         */
        public String getName() {
            return _name;
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
    private String _mqttControlTopic = StringUtils.EMPTY;
    private String _mqttStatusTopic = StringUtils.EMPTY;
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
     * @see Thing#getMqttControlTopic()
     */
    @Override
    public String getMqttControlTopic() {
        return _mqttControlTopic;
    }

    /**
     * (non-Javadoc)
     * @see Thing#setMqttControlTopic(String)
     */
    @Override
    public void setMqttControlTopic(String topicName) {
        _mqttControlTopic = topicName;
    }

    /**
     * (non-Javadoc)
     * @see Thing#getMqttStatusTopic()
     */
    @Override
    public String getMqttStatusTopic() {
        return _mqttStatusTopic;
    }

    /**
     * (non-Javadoc)
     * @see Thing#setMqttStatusTopic(String)
     */
    @Override
    public void setMqttStatusTopic(String topicName) {
        _mqttStatusTopic = topicName;
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
        _mqttControlTopic = null;
        _mqttStatusTopic = null;
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
            notifyListeners(new ThermostatEvent(_name, _state, state));
            _state = state;
        }
    }

    /**
     *
     * @return
     */
    public ThermostatMode mode() {
        return _mode;
    }

    /**
     *
     * @param mode
     */
    public void setMode(ThermostatMode mode) {
        _mode = mode;
    }
}
