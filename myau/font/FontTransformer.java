package myau.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class FontTransformer {
    private static FontTransformer instance;
    private final Map<String, Font> fontCache = new HashMap<String, Font>();
    private String selectedFontName = "minecraft";
    private float selectedFontSize = 18.0f;

    private FontTransformer() {
        this.loadFonts();
    }

    public static FontTransformer getInstance() {
        if (instance == null) {
            instance = new FontTransformer();
        }
        return instance;
    }

    private void loadFonts() {
        this.loadFont("Arial", "/assets/myau/font/Arial.ttf");
        this.loadFont("ArialBold", "/assets/myau/font/ArialBold.ttf");
        this.loadFont("RobotoMedium", "/assets/myau/font/Roboto-Medium.ttf");
        this.loadFont("JetBrainsMono", "/assets/myau/font/jetbrains.ttf");
        this.loadFont("MicrosoftYaHei", "/assets/myau/font/msyh-regular.ttf");
        this.loadFont("MicrosoftYaHei Bold", "/assets/myau/font/msyh-bold.ttf");
        this.loadFont("RalewayExtraBold", "/assets/myau/font/raleway-extrabold.ttf");
        this.loadFont("RobotoBlack", "/assets/myau/font/roboto-black.ttf");
        this.loadFont("RobotoRegular", "/assets/myau/font/roboto-regular.ttf");
        this.loadFont("ESP", "/assets/myau/font/esp-1.ttf");
        this.loadFont("ESPBold", "/assets/myau/font/esp-bold-3.ttf");
        this.loadFont("ESPItalic", "/assets/myau/font/esp-ital-4.ttf");
        this.loadFont("ESPBoldItalic", "/assets/myau/font/esp-bdit-2.ttf");
        this.loadFont("Consolas", "/assets/myau/font/consola-1.ttf");
        this.loadFont("OpenSansBold", "/assets/myau/font/OpenSans-Bold.ttf");
        this.loadFont("OpenSansBoldItalic", "/assets/myau/font/OpenSans-BoldItalic.ttf");
        this.loadFont("OpenSansExtraBold", "/assets/myau/font/OpenSans-ExtraBold.ttf");
        this.loadFont("OpenSansExtraBoldItalic", "/assets/myau/font/OpenSans-ExtraBoldItalic.ttf");
        this.loadFont("OpenSansItalic", "/assets/myau/font/OpenSans-Italic.ttf");
        this.loadFont("OpenSansLight", "/assets/myau/font/OpenSans-Light.ttf");
        this.loadFont("OpenSansLightItalic", "/assets/myau/font/OpenSans-LightItalic.ttf");
        this.loadFont("OpenSansRegular", "/assets/myau/font/OpenSans-Regular.ttf");
        this.loadFont("OpenSansSemiBold", "/assets/myau/font/OpenSans-Semibold.ttf");
        this.loadFont("OpenSansSemiBoldItalic", "/assets/myau/font/OpenSans-SemiboldItalic.ttf");
        this.loadFont("SuperJoyful", "/assets/myau/font/Super Joyful.ttf");
        this.loadFont("Cheri", "/assets/myau/font/Cheri.ttf");
        this.loadFont("Cherl", "/assets/myau/font/Cherl.ttf");
        this.loadFont("Fortalesia", "/assets/myau/font/Fortalesia.ttf");
        this.loadFont("HarmonyOSRegular", "/assets/myau/font/HarmonyOS-Regular.ttf");
        this.loadFont("HarmonyOSBold", "/assets/myau/font/HarmonyOS-Bold.ttf");
        this.loadFont("HarmonyOSBlack", "/assets/myau/font/HarmonyOS-Black.ttf");
    }

    private void loadFont(String name, String path) {
        try {
            InputStream stream = FontTransformer.class.getResourceAsStream(path);
            if (stream != null) {
                Font font = Font.createFont(0, stream);
                this.fontCache.put(name, font);
                stream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setFont(String fontName, float size) {
        this.selectedFontName = fontName;
        this.selectedFontSize = size;
    }

    public String getSelectedFontName() {
        return this.selectedFontName;
    }

    public float getSelectedFontSize() {
        return this.selectedFontSize;
    }

    public Font getFont(String fontName, float size) {
        if ("minecraft".equals(fontName)) {
            return null;
        }
        Font base = this.fontCache.get(fontName);
        if (base == null) {
            return null;
        }
        return base.deriveFont(size * 0.5f);
    }

    public boolean isMinecraftFont() {
        return "minecraft".equals(this.selectedFontName);
    }

    public String[] getAvailableFonts() {
        String[] fonts = new String[this.fontCache.size() + 1];
        fonts[0] = "minecraft";
        int i = 1;
        for (String name : this.fontCache.keySet()) {
            fonts[i++] = name;
        }
        return fonts;
    }

    public int getStringWidth(String text, String fontName, float size) {
        if ("minecraft".equals(fontName)) {
            return Minecraft.func_71410_x().field_71466_p.func_78256_a(text);
        }
        Font font = this.fontCache.get(fontName);
        if (font == null) {
            return Minecraft.func_71410_x().field_71466_p.func_78256_a(text);
        }
        Font derived = font.deriveFont(size * 0.5f);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        Rectangle2D bounds = derived.getStringBounds(text, frc);
        return (int)Math.round(bounds.getWidth());
    }

    public int getFontHeight(String fontName, float size) {
        if ("minecraft".equals(fontName)) {
            return Minecraft.func_71410_x().field_71466_p.field_78288_b;
        }
        Font font = this.fontCache.get(fontName);
        if (font == null) {
            return Minecraft.func_71410_x().field_71466_p.field_78288_b;
        }
        Font derived = font.deriveFont(size * 0.5f);
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
        Rectangle2D bounds = derived.getStringBounds("A", frc);
        return (int)Math.round(bounds.getHeight());
    }
}
