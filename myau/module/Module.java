package myau.module;

import java.util.ArrayList;
import java.util.List;
import myau.Myau;
import myau.module.modules.HUD;
import myau.module.modules.NotificationModule;
import myau.property.Property;
import myau.util.KeyBindUtil;

public abstract class Module {
    protected final String name;
    protected final boolean defaultEnabled;
    protected final int defaultKey;
    protected final boolean defaultHidden;
    protected boolean enabled;
    protected int key;
    protected boolean hidden;
    protected final List<Property<?>> properties = new ArrayList();

    public Module(String name, boolean enabled) {
        this(name, enabled, false);
    }

    public Module(String name, boolean enabled, boolean hidden) {
        this.name = name;
        this.enabled = this.defaultEnabled = enabled;
        this.defaultKey = 0;
        this.key = 0;
        this.hidden = this.defaultHidden = hidden;
    }

    public String getName() {
        return this.name;
    }

    public String formatModule() {
        return String.format("%s%s &r(%s&r)", this.key == 0 ? "" : String.format("&l[%s] &r", KeyBindUtil.getKeyName(this.key)), this.name, this.enabled ? "&a&lON" : "&c&lOFF");
    }

    public String[] getSuffix() {
        return new String[0];
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean getState() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                this.onEnabled();
            } else {
                this.onDisabled();
            }
        }
    }

    public boolean toggle() {
        boolean enabled = !this.enabled;
        this.setEnabled(enabled);
        if (this.enabled == enabled) {
            NotificationModule notificationModule;
            if (((Boolean)((HUD)Myau.moduleManager.modules.get(HUD.class)).toggleSound.getValue()).booleanValue()) {
                Myau.moduleManager.playSound();
            }
            if ((notificationModule = (NotificationModule)Myau.moduleManager.modules.get(NotificationModule.class)) != null && notificationModule.enabled) {
                String status = enabled ? "Enabled" : "Disabled";
                NotificationModule.MANAGER.add(this.name + " " + status);
            }
            return true;
        }
        return false;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int integer) {
        this.key = integer;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean boolean1) {
        this.hidden = boolean1;
    }

    protected void addProperty(Property<?> property) {
        this.properties.add(property);
    }

    public void onEnabled() {
    }

    public void onDisabled() {
    }

    public void verifyValue(String string) {
    }
}
