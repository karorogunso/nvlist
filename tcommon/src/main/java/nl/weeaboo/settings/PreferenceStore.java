package nl.weeaboo.settings;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

public class PreferenceStore implements IPreferenceStore {

    private final Map<String, Var> map = new HashMap<String, Var>();
    private final List<IPreferenceListener> listeners = new CopyOnWriteArrayList<IPreferenceListener>();
    private final Queue<FireEvent<?>> fireQueue = new ArrayDeque<FireEvent<?>>();

    public PreferenceStore() {
    }

    @Override
    public void init(Map<String, String> userOverrides) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadVariables() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveVariables() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPreferenceListener(IPreferenceListener pl) {
        listeners.add(pl);
    }

    @Override
    public void removePreferenceListener(IPreferenceListener pl) {
        listeners.remove(pl);
    }

    protected <T> void fireChanged(Preference<T> pref, T oldValue, T newValue) {
        fireQueue.add(new FireEvent<T>(pref, oldValue, newValue));
        if (fireQueue.size() > 1) {
            return; // Some other event is still firing
        }

        // Keep firing events until the queue is empty
        while (!fireQueue.isEmpty()) {
            FireEvent<?> event = fireQueue.remove();
            event.fire(listeners);
        }
    }

    @Override
    public <T> T get(Preference<T> pref) {
        Var var = map.get(pref.getKey());
        return (var != null ? var.getValue(pref) : pref.getDefaultValue());
    }

    @Override
    public <T, V extends T> void set(Preference<T> pref, V value) {
        T oldValue;

        Var var = map.get(pref.getKey());
        if (var != null) {
            oldValue = var.setValue(pref, value);
        } else {
            oldValue = pref.getDefaultValue();
            var = Var.fromProperty(pref, value);
            map.put(pref.getKey(), var);
        }

        if (oldValue != value && (oldValue == null || !oldValue.equals(value))) {
            // Only fire a change event if something has actually changed
            fireChanged(pref, oldValue, value);
        }
    }

    private final static class Var {

        private final boolean isConstant;
        private String raw;

        private transient Preference<?> lastGetterProp;
        private transient Object lastGetterValue;

        public Var(boolean isConstant, String raw) {
            this.isConstant = isConstant;
            this.raw = raw;
        }

        public static <T> Var fromProperty(Preference<T> property, T value) {
            Var var = new Var(property.isConstant(), null);
            var.setValue(property, value);
            return var;
        }

        public boolean isConstant() {
            return isConstant;
        }

        public String getRaw() {
            return raw;
        }

        public <T> T getValue(Preference<T> property) {
            Class<T> type = property.getType();
            if (property.equals(lastGetterProp)) {
                return type.cast(lastGetterValue); // Return cached value
            }

            T val = property.fromString(raw);
            if (!property.isValidValue(val)) {
                val = property.getDefaultValue();
            }

            lastGetterProp = property;
            lastGetterValue = val;

            return val;
        }

        public <T> T setValue(Preference<T> property, T value) {
            if (!property.isValidValue(value)) {
                throw new IllegalArgumentException(
                        "Invalid value for property: " + property.getKey() + " -> " + value);
            }
            if (isConstant || property.isConstant()) {
                throw new UnsupportedOperationException("Attempting to change the value of a constant: "
                        + property.getKey() + " -> " + value);
            }

            T oldValue = getValue(property);

            raw = property.toString(value);
            lastGetterProp = null;
            lastGetterValue = null;

            return oldValue;
        }

    }

    private static class FireEvent<T> {

        private final Preference<T> pref;
        private final T oldValue;
        private final T newValue;

        public FireEvent(Preference<T> pref, T oldValue, T newValue) {
            this.pref = pref;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public void fire(List<IPreferenceListener> listeners) {
            for (IPreferenceListener pl : listeners) {
                pl.onPreferenceChanged(pref, oldValue, newValue);
            }
        }

    }

}