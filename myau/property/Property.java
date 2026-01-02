package myau.property;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import myau.module.Module;

public abstract class Property<T> {
    private final String name;
    private final T type;
    private final Predicate<T> validator;
    private final BooleanSupplier visibleChecker;
    private T value;
    private Module owner;

    protected Property(String name, Object value, BooleanSupplier visibleChecker) {
        this(name, value, null, visibleChecker);
    }

    protected Property(String name, Object value, Predicate<T> predicate, BooleanSupplier visibleChecker) {
        this.name = name;
        this.type = value;
        this.validator = predicate;
        this.visibleChecker = visibleChecker;
        this.value = value;
        this.owner = null;
    }

    public String getName() {
        return this.name;
    }

    public abstract String getValuePrompt();

    public boolean isVisible() {
        return this.visibleChecker == null || this.visibleChecker.getAsBoolean();
    }

    public T getValue() {
        return this.value;
    }

    public abstract String formatValue();

    public boolean setValue(Object object) {
        if (this.validator != null && !this.validator.test(object)) {
            return false;
        }
        this.value = object;
        if (this.owner != null) {
            this.owner.verifyValue(this.name);
        }
        return true;
    }

    public void parseString() {
    }

    public void setOwner(Module module) {
        this.owner = module;
    }

    public abstract boolean parseString(String var1);

    public abstract boolean read(JsonObject var1);

    public abstract void write(JsonObject var1);
}
