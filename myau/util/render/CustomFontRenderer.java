package myau.util.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

public class CustomFontRenderer {
    private final DynamicTexture texture;
    private final CharData[] charData = new CharData[256];
    private final int height;

    public CustomFontRenderer(String fontPath, int size) {
        try (InputStream is = this.getClass().getResourceAsStream(fontPath);){
            if (is == null) {
                throw new RuntimeException("Font not found: " + fontPath);
            }
            Font baseFont = Font.createFont(0, is).deriveFont(0, size * 2);
            this.texture = this.generateTexture(baseFont);
            this.height = (int)baseFont.getSize2D();
        }
        catch (FontFormatException | IOException e) {
            throw new RuntimeException("Failed to load font: " + fontPath, e);
        }
    }

    public CustomFontRenderer(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Font cannot be null");
        }
        Font scaledFont = font.deriveFont(font.getSize2D() * 2.0f);
        this.texture = this.generateTexture(scaledFont);
        this.height = (int)scaledFont.getSize2D();
    }

    private DynamicTexture generateTexture(Font font) {
        BufferedImage img = new BufferedImage(512, 512, 2);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int x = 2;
        int y = lineHeight;
        for (int i = 0; i < 256; ++i) {
            char c = (char)i;
            if (c == '\n' || c == '\r') {
                this.charData[i] = null;
                continue;
            }
            int charWidth = fm.charWidth(c);
            if (charWidth <= 0) {
                charWidth = 1;
            }
            if (x + charWidth > 510) {
                x = 2;
                if ((y += lineHeight) + lineHeight > 510) break;
            }
            this.charData[i] = new CharData();
            this.charData[i].x = x;
            this.charData[i].y = y;
            this.charData[i].width = charWidth;
            this.charData[i].height = lineHeight;
            g.drawString(String.valueOf(c), x, y);
            x += charWidth + 1;
        }
        g.dispose();
        return new DynamicTexture(img);
    }

    public void drawString(String text, float x, float y, int color) {
        GL11.glEnable((int)3553);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glBindTexture((int)3553, (int)this.texture.func_110552_b());
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        float currentX = x;
        for (char c : text.toCharArray()) {
            if (c >= '\u0100' || this.charData[c] == null) continue;
            CharData cd = this.charData[c];
            float u1 = (float)cd.x / 512.0f;
            float v1 = (float)cd.y / 512.0f;
            float u2 = (float)(cd.x + cd.width) / 512.0f;
            float v2 = (float)(cd.y + cd.height) / 512.0f;
            GL11.glBegin((int)7);
            GL11.glTexCoord2f((float)u1, (float)v1);
            GL11.glVertex2f((float)currentX, (float)y);
            GL11.glTexCoord2f((float)u1, (float)v2);
            GL11.glVertex2f((float)currentX, (float)(y + (float)cd.height));
            GL11.glTexCoord2f((float)u2, (float)v2);
            GL11.glVertex2f((float)(currentX + (float)cd.width), (float)(y + (float)cd.height));
            GL11.glTexCoord2f((float)u2, (float)v1);
            GL11.glVertex2f((float)(currentX + (float)cd.width), (float)y);
            GL11.glEnd();
            currentX += (float)cd.width;
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)3553);
    }

    public int getStringWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            if (c >= '\u0100' || this.charData[c] == null) continue;
            width += this.charData[c].width;
        }
        return width;
    }

    public int getFontHeight() {
        return this.height;
    }

    private static class CharData {
        public int x;
        public int y;
        public int width;
        public int height;

        private CharData() {
        }
    }
}
