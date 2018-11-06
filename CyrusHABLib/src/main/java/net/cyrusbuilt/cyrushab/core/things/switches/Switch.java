package net.cyrusbuilt.cyrushab.core.things.switches;

import net.cyrusbuilt.cyrushab.core.Disposable;
import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstraction of a switch/button or relay or a device that can be switched on/off.
 */
public abstract class Switch implements Thing {
    /**
     * A switch state change event.
     */
    public static class SwitchEvent {
        private SwitchState _oldState = SwitchState.OFF;
        private SwitchState _newState = SwitchState.OFF;
        private String _name = StringUtils.EMPTY;

        /**
         * Creates a new instance of Switch.SwitchEvent with the old and new states, and device name.
         * @param oldState The previous state of the switch.
         * @param newState The new (current) state of the switch.
         * @param name The switch name.
         */
        public SwitchEvent(SwitchState oldState, SwitchState newState, String name) {
            _oldState = oldState;
            _newState = newState;
            _name = name;
        }

        /**
         * Gets the previous state of the switch.
         * @return The previous state.
         */
        public SwitchState oldState() {
            return _oldState;
        }

        /**
         * Gets the new (current) state of the switch.
         * @return The current state.
         */
        public SwitchState newState() {
            return _newState;
        }

        /**
         * Gets the name of the switch.
         * @return The name of the switch.
         */
        public String name() {
            return _name;
        }
    }

    /**
     * The event listener interface for switch state change events.
     */
    public interface OnSwitchStateChangeListener {
        /**
         * Handles switch state change events.
         * @param event The state change info.
         */
        void onStateChanged(SwitchEvent event);
    }

    private String _name = StringUtils.EMPTY;
    private String _clientID = StringUtils.EMPTY;
    private Object _tag = null;
    private boolean _isDisposed = false;
    private volatile SwitchState _state = SwitchState.OFF;
    private List<OnSwitchStateChangeListener> _listeners;
    private boolean _isReadonly = true;
    private boolean _enabled = true;
    private int _id = -1;

    /**
     * Creates a new instance of {@link Switch} (default constructor).
     */
    protected Switch() {
        _listeners = new ArrayList<>();
    }

    /**
     * Creates a new instance of {@link Switch} with the device name.
     * @param name The name of the device.
     */
    protected Switch(String name) {
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
        return ThingType.SWITCH;
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
    public void setIsReadonly(boolean readonly) {
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
    public void setEnabled(boolean enabled) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(Switch.class.getCanonicalName());
        }
        _enabled = enabled;
    }

    /**
     * Adds an event listener for this Switch instance.
     * @param listener The listener to add.
     * @throws ObjectDisposedException if this instance is disposed.
     */
    public void addListener(@NotNull OnSwitchStateChangeListener listener) throws ObjectDisposedException {
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
    public void notifyListeners(@NotNull SwitchEvent event) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(this.getClass().getCanonicalName());
        }

        for (OnSwitchStateChangeListener listener : _listeners) {
            listener.onStateChanged(event);
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

        _name = null;
        _tag = null;
        _state = SwitchState.OFF;
        _enabled = false;
        _isReadonly = false;
        _isDisposed = true;
    }

    /**
     * Gets the current state of the switch.
     * @return The current state.
     */
    public synchronized SwitchState state() {
        return _state;
    }

    /**
     * Sets the state of this switch.
     * @param state The new state.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    protected synchronized void setState(SwitchState state) throws ObjectDisposedException {
        if (_state != state) {
            notifyListeners(new SwitchEvent(_state, state, _name));
            _state = state;
        }
    }

    /**
     * Gets whether the Swith is "On".
     * @return true if "On"; Otherwise, false.
     */
    public boolean isOn() {
        return _state == SwitchState.ON;
    }

    /**
     * Gets whether this Switch is "Off".
     * @return true if "Off"; Otherwise, false.
     */
    public boolean isOff() {
        return _state == SwitchState.OFF;
    }

    /**
     * Maps pertinent state values from the specified status packet.
     * @param packet The status packet.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void mapFromStatusPacket(@NotNull SwitchStatusPacket packet) throws ObjectDisposedException {
        setEnabled(packet.isEnabled());
        setIsReadonly(packet.isReadonly());
        setState(packet.getState());
    }
}
