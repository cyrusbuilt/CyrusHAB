package net.cyrusbuilt.cyrushab.core.things;

import org.apache.commons.lang3.StringUtils;

public class Switch implements Thing {
    private String _name = StringUtils.EMPTY;
    private Object _tag = null;
    private final ThingType _type = ThingType.SWITCH;
    private boolean _isDisposed = false;

    public Switch() {

    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public void setName(final String name) {
        _name = name;
    }

    @Override
    public Object tag() {
        return _tag;
    }

    @Override
    public void setTag(final Object tag) {
        _tag = tag;
    }

    @Override
    public ThingType type() {
        return _type;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        if (_isDisposed) {
            return;
        }

        _name = null;
        _tag = null;
        _isDisposed = true;
    }
}
