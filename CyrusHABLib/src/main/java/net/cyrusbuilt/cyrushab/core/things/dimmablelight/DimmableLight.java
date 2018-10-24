package net.cyrusbuilt.cyrushab.core.things.dimmablelight;

import net.cyrusbuilt.cyrushab.core.Disposable;
import net.cyrusbuilt.cyrushab.core.ObjectDisposedException;
import net.cyrusbuilt.cyrushab.core.things.Thing;
import net.cyrusbuilt.cyrushab.core.things.ThingType;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstraction of a dimmable light or dimmer switch.
 */
public abstract class DimmableLight implements Thing {
    /**
     * A dimmable light state change event.
     */
    public static class DimmableEvent {
        private int _level = 0;
        private int _minLevel = 0;
        private int _maxLevel = 0;
        private boolean _isOn = false;

        /**
         * Constructs a new instance of {@link DimmableEvent} with the minimum level, max level, and current level.
         * @param level The current light level.
         * @param minLevel The minimum light level.
         * @param maxLevel The maximum light level.
         * @param isOn Set true if the light level is high enough to consider the light "on".
         */
        public DimmableEvent(int level, int minLevel, int maxLevel, boolean isOn) {
            if (minLevel < 0) {
                minLevel = 0;
            }

            if (minLevel > 255) {
                minLevel = 255;
            }

            if (maxLevel < 0) {
                maxLevel = 0;
            }

            if (maxLevel > 255) {
                maxLevel = 255;
            }

            _level = level;
            _minLevel = minLevel;
            _maxLevel = maxLevel;
            _isOn = isOn;
        }

        /**
         * Gets the current light level
         * @return The current level.
         */
        public int level() {
            return _level;
        }

        /**
         * Gets the minimum light level.
         * @return The minimum level.
         */
        public int minLevel() {
            return _minLevel;
        }

        /**
         * Gets the maximum light level.
         * @return The max level.
         */
        public int maxLevel() {
            return _maxLevel;
        }

        /**
         * Gets whether or not the light level is high enough to consider the light to be "on".
         * @return true if the light is on; Otherwise, false.
         */
        public boolean isOn() {
            return _isOn;
        }
    }

    /**
     * The event listener interface for dimmable light state change events.
     */
    public interface OnDimmableLightStateChangeListener {
        /**
         * Handle dimmable light state change events.
         * @param event The event info.
         */
        void onDimmableStateChangeEvent(DimmableEvent event);
    }

    private static final Object _lock = new Object();
    private String _name = StringUtils.EMPTY;
    private Object _tag = null;
    private boolean _isDisposed = false;
    private boolean _isReadonly = true;
    private boolean _enabled = true;
    private int _id = -1;
    private volatile int _level = 0;
    private int _minLevel = 0;
    private int _maxLevel = 0;
    private List<OnDimmableLightStateChangeListener> _listeners;

    /**
     * Creates a new instance of {@link DimmableLight} (default constructor).
     */
    protected DimmableLight() {
        _listeners = new ArrayList<>();
    }

    /**
     * Creates a new instance of {@link DimmableLight} with the device name.
     * @param name The device name.
     */
    protected DimmableLight(String name) {
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
        return ThingType.DIMMABLE_LIGHT;
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
     * Gets the current light level.
     * @return The current level.
     */
    public synchronized int level() {
        return _level;
    }

    /**
     * Gets the minimum light level.
     * @return The minimum level.
     */
    public int minLevel() {
        return _minLevel;
    }

    /**
     * Gets the maximum light level.
     * @return The max level.
     */
    public int maxLevel() {
        return _maxLevel;
    }

    /**
     * Gets whether or not the light is on.
     * @return true if the light is on.
     */
    public boolean isOn() {
        return level() > _minLevel;
    }

    /**
     * Gets whether the light is off.
     * @return true if the light is off.
     */
    public boolean isOff() {
        return level() <= _maxLevel;
    }

    /**
     * Sets the light level.
     * @param level The level to set. Must be between {@link #minLevel()} and {@link #maxLevel()}.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public synchronized void setLevel(int level) throws ObjectDisposedException {
        if (level < _minLevel) {
            level = _minLevel;
        }

        if (level > _maxLevel) {
            level = _maxLevel;
        }

        if (_level != level) {
            notifyListeners(new DimmableEvent(level, minLevel(), maxLevel(), isOn()));
            _level = level;
        }
    }

    /**
     * Turns the light on by setting the light level to max.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void turnOn() throws ObjectDisposedException {
        setLevel(maxLevel());
    }

    /**
     * Turns the light off by setting the light level to minimum.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    public void turnOff() throws ObjectDisposedException {
        setLevel(minLevel());
    }

    /**
     * Sets whether this device is enabled.
     * @param enabled Set true to enable.
     * @throws ObjectDisposedException if this instance has been disposed.
     */
    protected void setEnabled(boolean enabled) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(DimmableLight.class.getCanonicalName());
        }
        _enabled = enabled;
    }

    /**
     * Adds an event listener for this dimmable light instance.
     * @param listener The listener to add.
     * @throws ObjectDisposedException if this instance is disposed.
     */
    public void addListener(OnDimmableLightStateChangeListener listener) throws ObjectDisposedException {
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
     * @throws ObjectDisposedException if this instance is disposed.
     */
    public void notifyListeners(DimmableEvent event) throws ObjectDisposedException {
        if (isDisposed()) {
            throw new ObjectDisposedException(this.getClass().getCanonicalName());
        }

        for (OnDimmableLightStateChangeListener listener : _listeners) {
            listener.onDimmableStateChangeEvent(event);
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
            _level = 0;
        }

        _name = null;
        _tag = null;
        _minLevel = 0;
        _maxLevel = 0;
        _enabled = false;
        _isReadonly = false;
        _isDisposed = true;
    }

    /**
     * Gets the current light level percentage.
     * @return The light level percentage.
     */
    public int getLevelPercentage() {
        int level = level();
        int minVal = Math.min(minLevel(), maxLevel());
        int maxVal = Math.max(minLevel(), maxLevel());
        int range = (maxVal - minVal);
        return (level * 100) / range;
    }
}
