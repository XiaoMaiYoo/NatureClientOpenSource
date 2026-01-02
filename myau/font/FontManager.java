package myau.font;

import java.util.HashMap;
import java.util.Map;
import myau.util.render.CustomFontRenderer;

public class FontManager {
    public static final FontManager INSTANCE = new FontManager();
    private final Map<String, CustomFontRenderer> fontCache = new HashMap<String, CustomFontRenderer>();
    private boolean enabled = false;

    public void setFont(String fontName, float size) {
        if (fontName == null || fontName.equalsIgnoreCase("MINECRAFT")) {
            this.enabled = false;
            return;
        }
        this.enabled = true;
        if (!this.fontCache.containsKey(fontName)) {
            try {
                CustomFontRenderer renderer = new CustomFontRenderer("/assets/myau/font/" + fontName + ".ttf", (int)size);
                this.fontCache.put(fontName, renderer);
            }
            catch (Exception e) {
                e.printStackTrace();
                this.enabled = false;
            }
        }
    }

    public CustomFontRenderer getFontRenderer() {
        return this.enabled ? this.fontCache.get(this.getCurrentFontName()) : null;
    }

    public boolean isEnabled() {
        return this.enabled && this.getFontRenderer() != null;
    }

    public String getCurrentFontName() {
        return this.enabled ? this.fontCache.keySet().iterator().next() : "MINECRAFT";
    }
}
