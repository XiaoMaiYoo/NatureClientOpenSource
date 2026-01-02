package myau.module.modules;

import myau.font.FontTransformer;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;

public class GlobalFont
extends Module {
    private final FontTransformer fontTransformer = FontTransformer.getInstance();
    public final ModeProperty fontType = new ModeProperty("Font", 0, this.fontTransformer.getAvailableFonts());
    public final FloatProperty fontSize = new FloatProperty("Size", Float.valueOf(30.0f), Float.valueOf(18.0f), Float.valueOf(120.0f));

    public GlobalFont() {
        super("GlobalFont", false, false);
    }

    @Override
    public void onEnabled() {
        this.updateFont();
    }

    @Override
    public void onDisabled() {
        this.fontTransformer.setFont("minecraft", 18.0f);
    }

    @Override
    public void verifyValue(String valueName) {
        if (this.isEnabled()) {
            this.updateFont();
        }
    }

    private void updateFont() {
        String fontName = this.fontType.getModeString();
        float size = ((Float)this.fontSize.getValue()).floatValue();
        this.fontTransformer.setFont(fontName, size);
    }

    public String getCurrentFontName() {
        return this.isEnabled() ? this.fontType.getModeString() : "minecraft";
    }

    public float getCurrentFontSize() {
        return this.isEnabled() ? ((Float)this.fontSize.getValue()).floatValue() : 18.0f;
    }

    public boolean isUsingCustomFont() {
        return this.isEnabled() && !this.fontTransformer.isMinecraftFont();
    }
}
