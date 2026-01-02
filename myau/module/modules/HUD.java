package myau.module.modules;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.Render2DEvent;
import myau.events.TickEvent;
import myau.font.FontTransformer;
import myau.mixin.IAccessorGuiChat;
import myau.module.Module;
import myau.module.modules.GlobalFont;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.RenderUtil;
import myau.util.render.CustomFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class HUD
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private List<Module> activeModules = new ArrayList<Module>();
    public final ModeProperty fontMode = new ModeProperty("font", 0, new String[]{"MINECRAFT", "GOOGLE_SANS"});
    public final ColorProperty gradientStart = new ColorProperty("gradient-start", new Color(102, 255, 102).getRGB());
    public final ColorProperty gradientEnd = new ColorProperty("gradient-end", new Color(50, 150, 255).getRGB());
    public final BooleanProperty glowEffect = new BooleanProperty("glow-effect", true);
    public final ModeProperty posX = new ModeProperty("position-x", 1, new String[]{"LEFT", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 0, new String[]{"TOP", "BOTTOM"});
    public final BooleanProperty toggleSound = new BooleanProperty("toggle-sounds", true);
    public final BooleanProperty toggleAlerts = new BooleanProperty("toggle-alerts", false);
    public final IntProperty offsetX = new IntProperty("offset-x", 5, 0, 255);
    public final IntProperty offsetY = new IntProperty("offset-y", 5, 0, 255);
    public final FloatProperty scale = new FloatProperty("scale", Float.valueOf(1.1f), Float.valueOf(0.8f), Float.valueOf(2.0f));
    public final IntProperty backgroundAlpha = new IntProperty("background-alpha", 90, 0, 255);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final BooleanProperty suffixes = new BooleanProperty("suffixes", true);
    public final BooleanProperty lowerCase = new BooleanProperty("lower-case", false);
    public final BooleanProperty chatOutline = new BooleanProperty("chat-outline", true);
    public final BooleanProperty blinkTimer = new BooleanProperty("blink-timer", true);
    public final BooleanProperty blurBackground = new BooleanProperty("blur-background", true);
    public final IntProperty blurRadius = new IntProperty("blur-radius", 8, 1, 15);
    public final BooleanProperty roundedCorners = new BooleanProperty("rounded-corners", true);
    public final IntProperty cornerRadius = new IntProperty("corner-radius", 8, 5, 20);
    public final BooleanProperty showBar = new BooleanProperty("bar", false);
    public final IntProperty spacing = new IntProperty("spacing", 2, 0, 10);
    public final BooleanProperty multiLayerGlow = new BooleanProperty("multi-layer-glow", true);
    public final IntProperty glowLayers = new IntProperty("glow-layers", 3, 1, 8);
    public final FloatProperty glowSpread = new FloatProperty("glow-spread", Float.valueOf(0.5f), Float.valueOf(0.1f), Float.valueOf(2.0f));
    public final IntProperty glowAlpha = new IntProperty("glow-alpha", 40, 10, 100);
    public final BooleanProperty fastWatermark = new BooleanProperty("fast-watermark", true);
    public final ModeProperty watermarkText = new ModeProperty("watermark-text", 0, new String[]{"Nature", "Raven", "Myau", "Client", "Custom"});
    public final ModeProperty watermarkColorMode = new ModeProperty("watermark-color-mode", 0, new String[]{"CUSTOM", "BLUE", "YELLOW", "RED", "RAINBOW", "GRADIENT"});
    public final ColorProperty watermarkCustomColor = new ColorProperty("watermark-custom-color", new Color(102, 255, 102).getRGB());
    public final ColorProperty watermarkGradientStart = new ColorProperty("gradient-start-color", new Color(102, 255, 102).getRGB());
    public final ColorProperty watermarkGradientEnd = new ColorProperty("gradient-end-color", new Color(50, 150, 255).getRGB());
    public final BooleanProperty belugMode = new BooleanProperty("Belug", false);
    private CustomFontRenderer currentCustomFont = null;
    private String lastFontName = "";
    private float lastFontSize = -1.0f;
    private List<String> moduleActivationOrder = new ArrayList<String>();

    public HUD() {
        super("HUD", true, true);
    }

    private String getModuleName(Module module) {
        String name = module.getName();
        return (Boolean)this.lowerCase.getValue() != false ? name.toLowerCase(Locale.ROOT) : name;
    }

    private String[] getModuleSuffix(Module module) {
        String[] suffixes = module.getSuffix();
        if (((Boolean)this.lowerCase.getValue()).booleanValue() && suffixes != null) {
            for (int i = 0; i < suffixes.length; ++i) {
                suffixes[i] = suffixes[i].toLowerCase(Locale.ROOT);
            }
        }
        return suffixes;
    }

    private int getStringWidth(Module module, CustomFontRenderer customFont) {
        String name = this.getModuleName(module);
        String[] suf = (Boolean)this.suffixes.getValue() != false ? this.getModuleSuffix(module) : null;
        int width = customFont != null ? customFont.getStringWidth(name) : HUD.mc.field_71466_p.func_78256_a(name);
        if (suf != null) {
            for (String s : suf) {
                if (customFont != null) {
                    width += 3 + customFont.getStringWidth(s);
                    continue;
                }
                width += 3 + HUD.mc.field_71466_p.func_78256_a(s);
            }
        }
        return width;
    }

    private int getFontHeight(CustomFontRenderer customFont) {
        return customFont != null ? customFont.getFontHeight() : HUD.mc.field_71466_p.field_78288_b;
    }

    private Color getGradientColor(int index, int total) {
        if (total <= 1) {
            return new Color((Integer)this.gradientStart.getValue());
        }
        float t = (float)index / (float)(total - 1);
        Color start = new Color((Integer)this.gradientStart.getValue());
        Color end = new Color((Integer)this.gradientEnd.getValue());
        int r = (int)((float)start.getRed() + t * (float)(end.getRed() - start.getRed()));
        int g = (int)((float)start.getGreen() + t * (float)(end.getGreen() - start.getGreen()));
        int b = (int)((float)start.getBlue() + t * (float)(end.getBlue() - start.getBlue()));
        return new Color(r, g, b);
    }

    private Color getWatermarkColor(float offset) {
        switch ((Integer)this.watermarkColorMode.getValue()) {
            case 0: {
                return new Color((Integer)this.watermarkCustomColor.getValue());
            }
            case 1: {
                return Color.BLUE;
            }
            case 2: {
                return Color.YELLOW;
            }
            case 3: {
                return Color.RED;
            }
            case 4: {
                float hue = (float)(System.currentTimeMillis() % 5000L) / 5000.0f + offset;
                return Color.getHSBColor(hue % 1.0f, 0.8f, 1.0f);
            }
            case 5: {
                float t = (float)(System.currentTimeMillis() % 3000L) / 3000.0f;
                Color start = new Color((Integer)this.watermarkGradientStart.getValue());
                Color end = new Color((Integer)this.watermarkGradientEnd.getValue());
                int r = (int)((float)start.getRed() + t * (float)(end.getRed() - start.getRed()));
                int g = (int)((float)start.getGreen() + t * (float)(end.getGreen() - start.getGreen()));
                int b = (int)((float)start.getBlue() + t * (float)(end.getBlue() - start.getBlue()));
                return new Color(r, g, b);
            }
        }
        return Color.WHITE;
    }

    private void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        if (!((Boolean)this.roundedCorners.getValue()).booleanValue() || radius <= 0.0f) {
            RenderUtil.drawRect(x, y, x + w, y + h, color);
            return;
        }
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        GlStateManager.func_179147_l();
        GlStateManager.func_179090_x();
        GlStateManager.func_179120_a((int)770, (int)771, (int)1, (int)0);
        GlStateManager.func_179131_c((float)red, (float)green, (float)blue, (float)alpha);
        GL11.glBegin((int)7);
        GL11.glVertex2f((float)(x + radius), (float)y);
        GL11.glVertex2f((float)(x + w - radius), (float)y);
        GL11.glVertex2f((float)(x + w - radius), (float)(y + h));
        GL11.glVertex2f((float)(x + radius), (float)(y + h));
        GL11.glVertex2f((float)x, (float)(y + radius));
        GL11.glVertex2f((float)(x + radius), (float)(y + radius));
        GL11.glVertex2f((float)(x + radius), (float)(y + h - radius));
        GL11.glVertex2f((float)x, (float)(y + h - radius));
        GL11.glVertex2f((float)(x + w - radius), (float)(y + radius));
        GL11.glVertex2f((float)(x + w), (float)(y + radius));
        GL11.glVertex2f((float)(x + w), (float)(y + h - radius));
        GL11.glVertex2f((float)(x + w - radius), (float)(y + h - radius));
        GL11.glEnd();
        this.drawArc(x + radius, y + radius, radius, 180.0f, 270.0f, red, green, blue, alpha);
        this.drawArc(x + w - radius, y + radius, radius, 270.0f, 360.0f, red, green, blue, alpha);
        this.drawArc(x + w - radius, y + h - radius, radius, 0.0f, 90.0f, red, green, blue, alpha);
        this.drawArc(x + radius, y + h - radius, radius, 90.0f, 180.0f, red, green, blue, alpha);
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
    }

    private void drawArc(float cx, float cy, float r, float start, float end, float red, float green, float blue, float alpha) {
        GlStateManager.func_179131_c((float)red, (float)green, (float)blue, (float)alpha);
        GL11.glBegin((int)6);
        GL11.glVertex2f((float)cx, (float)cy);
        for (float i = start; i <= end; i += 5.0f) {
            double a = Math.toRadians(i);
            GL11.glVertex2f((float)((float)((double)cx + Math.cos(a) * (double)r)), (float)((float)((double)cy + Math.sin(a) * (double)r)));
        }
        GL11.glEnd();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            List newlyActive = Myau.moduleManager.modules.values().stream().filter(m -> m.isEnabled() && !m.isHidden()).collect(Collectors.toList());
            for (Module module : newlyActive) {
                String moduleName = module.getName();
                if (this.activeModules.contains(module) || this.moduleActivationOrder.contains(moduleName)) continue;
                this.moduleActivationOrder.add(moduleName);
            }
            ArrayList<String> toRemove = new ArrayList<String>();
            for (String moduleName : this.moduleActivationOrder) {
                Module module = Myau.moduleManager.modules.get(moduleName);
                if (module != null && module.isEnabled() && !module.isHidden()) continue;
                toRemove.add(moduleName);
            }
            this.moduleActivationOrder.removeAll(toRemove);
            this.activeModules = newlyActive.stream().sorted(Comparator.comparingInt(m -> -this.getStringWidth((Module)m, null))).collect(Collectors.toList());
        }
    }

    private int getModuleActivationIndex(String moduleName) {
        return this.moduleActivationOrder.indexOf(moduleName);
    }

    private float getModuleActivationWeight(String moduleName) {
        int index = this.getModuleActivationIndex(moduleName);
        if (index == -1) {
            return 0.0f;
        }
        return 1.0f - (float)index / (float)Math.max(1, this.moduleActivationOrder.size() - 1);
    }

    private void drawMultiLayerText(String text, float x, float y, Color baseColor, float weight) {
        if (!((Boolean)this.multiLayerGlow.getValue()).booleanValue() || (Integer)this.glowLayers.getValue() <= 1) {
            HUD.mc.field_71466_p.func_175063_a(text, x, y, baseColor.getRGB());
            return;
        }
        int layers = (int)((float)((Integer)this.glowLayers.getValue()).intValue() * (0.5f + 0.5f * weight));
        float spread = ((Float)this.glowSpread.getValue()).floatValue() * (0.5f + 0.5f * weight);
        int alpha = (int)((float)((Integer)this.glowAlpha.getValue()).intValue() * (0.5f + 0.5f * weight));
        alpha = Math.max(10, Math.min(100, alpha));
        int originalAlpha = baseColor.getAlpha();
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();
        for (int i = layers - 1; i >= 0; --i) {
            float offset = (float)(i + 1) * spread;
            float currentAlpha = (float)(alpha * (layers - i)) / (float)layers;
            int layerColor = (int)currentAlpha << 24 | r << 16 | g << 8 | b;
            HUD.mc.field_71466_p.func_175063_a(text, x - offset, y, layerColor);
            HUD.mc.field_71466_p.func_175063_a(text, x + offset, y, layerColor);
            HUD.mc.field_71466_p.func_175063_a(text, x, y - offset, layerColor);
            HUD.mc.field_71466_p.func_175063_a(text, x, y + offset, layerColor);
            float diagonalOffset = offset * 0.707f;
            HUD.mc.field_71466_p.func_175063_a(text, x - diagonalOffset, y - diagonalOffset, layerColor);
            HUD.mc.field_71466_p.func_175063_a(text, x + diagonalOffset, y - diagonalOffset, layerColor);
            HUD.mc.field_71466_p.func_175063_a(text, x - diagonalOffset, y + diagonalOffset, layerColor);
            HUD.mc.field_71466_p.func_175063_a(text, x + diagonalOffset, y + diagonalOffset, layerColor);
        }
        HUD.mc.field_71466_p.func_175063_a(text, x, y, baseColor.getRGB());
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent event) {
        long packets;
        BlinkModules blinking;
        ScaledResolution sr;
        if (((Boolean)this.chatOutline.getValue()).booleanValue() && HUD.mc.field_71462_r instanceof GuiChat) {
            String text = ((IAccessorGuiChat)HUD.mc.field_71462_r).getInputField().func_146179_b().trim();
            if (Myau.commandManager != null && Myau.commandManager.isTypingCommand(text)) {
                RenderUtil.enableRenderState();
                RenderUtil.drawOutlineRect(2.0f, HUD.mc.field_71462_r.field_146295_m - 14, HUD.mc.field_71462_r.field_146294_l - 2, HUD.mc.field_71462_r.field_146295_m - 2, 1.5f, 0, (Integer)this.gradientStart.getValue());
                RenderUtil.disableRenderState();
            }
        }
        if (!this.isEnabled() || HUD.mc.field_71474_y.field_74330_P) {
            return;
        }
        CustomFontRenderer fontToUse = null;
        GlobalFont globalFontModule = (GlobalFont)Myau.moduleManager.modules.get(GlobalFont.class);
        if (globalFontModule != null && globalFontModule.isUsingCustomFont()) {
            String fontName = globalFontModule.getCurrentFontName();
            float fontSize = globalFontModule.getCurrentFontSize();
            if (!fontName.equals(this.lastFontName) || Math.abs(fontSize - this.lastFontSize) > 0.1f) {
                try {
                    Font awtFont = FontTransformer.getInstance().getFont(fontName, fontSize);
                    if (awtFont != null) {
                        this.currentCustomFont = new CustomFontRenderer(awtFont);
                        this.lastFontName = fontName;
                        this.lastFontSize = fontSize;
                    } else {
                        this.currentCustomFont = null;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    this.currentCustomFont = null;
                }
            }
            fontToUse = this.currentCustomFont;
        } else {
            this.currentCustomFont = null;
        }
        if (((Boolean)this.fastWatermark.getValue()).booleanValue()) {
            sr = new ScaledResolution(mc);
            float scale = ((Float)this.scale.getValue()).floatValue();
            String[] textOptions = new String[]{"Nature", "Raven", "Myau", "Client", "Custom"};
            String prefix = textOptions[(Integer)this.watermarkText.getValue()];
            String time = String.format("%02d:%02d:%02d", System.currentTimeMillis() / 3600000L % 24L, System.currentTimeMillis() / 60000L % 60L, System.currentTimeMillis() / 1000L % 60L);
            int health = Math.max(0, Math.round(HUD.mc.field_71439_g.func_110143_aJ()));
            String healthStr = "\u2764" + health;
            String fullText = prefix + " | " + time + " | " + healthStr;
            float textWidth = HUD.mc.field_71466_p.func_78256_a(fullText);
            float textHeight = HUD.mc.field_71466_p.field_78288_b;
            float x = 4.0f;
            float y = 4.0f;
            int bgColor = new Color(10, 10, 10, 180).getRGB();
            this.drawRoundedRect(x, y, textWidth + 6.0f, textHeight + 2.0f, 5.0f, bgColor);
            float offsetX = 0.0f;
            for (int i = 0; i < fullText.length(); ++i) {
                String c = String.valueOf(fullText.charAt(i));
                float w = HUD.mc.field_71466_p.func_78256_a(c);
                Color color = this.getWatermarkColor((float)i * 0.05f);
                HUD.mc.field_71466_p.func_175063_a(c, x + 3.0f + offsetX, y + 1.0f, color.getRGB());
                offsetX += w;
            }
        }
        sr = new ScaledResolution(mc);
        float scaleFactor = ((Float)this.scale.getValue()).floatValue();
        float xBase = ((Integer)this.offsetX.getValue()).intValue();
        float yBase = ((Integer)this.offsetY.getValue()).intValue();
        if ((Integer)this.posX.getValue() == 1) {
            xBase = (float)sr.func_78326_a() - xBase;
        }
        if ((Integer)this.posY.getValue() == 1) {
            yBase = (float)sr.func_78328_b() - yBase - (float)this.getFontHeight(fontToUse) * scaleFactor;
        }
        float currentY = (Integer)this.posY.getValue() == 0 ? yBase : yBase - (float)((this.activeModules.size() - 1) * (this.getFontHeight(fontToUse) + (Integer)this.spacing.getValue()));
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)scaleFactor, (float)scaleFactor, (float)1.0f);
        float drawYBase = currentY / scaleFactor;
        for (int i = 0; i < this.activeModules.size(); ++i) {
            Module module = this.activeModules.get(i);
            String name = this.getModuleName(module);
            String[] suf = (Boolean)this.suffixes.getValue() != false ? this.getModuleSuffix(module) : null;
            int width = this.getStringWidth(module, fontToUse);
            Color color = this.getGradientColor(i, this.activeModules.size());
            float activationWeight = this.getModuleActivationWeight(module.getName());
            Color finalColor = color;
            if (((Boolean)this.multiLayerGlow.getValue()).booleanValue() && fontToUse == null && activationWeight > 0.1f) {
                float brightnessBoost = 0.1f * activationWeight;
                int r = Math.min(255, (int)((float)color.getRed() * (1.0f + brightnessBoost)));
                int g = Math.min(255, (int)((float)color.getGreen() * (1.0f + brightnessBoost)));
                int b = Math.min(255, (int)((float)color.getBlue() * (1.0f + brightnessBoost)));
                finalColor = new Color(r, g, b);
            }
            int colorInt = finalColor.getRGB();
            float drawX = xBase / scaleFactor;
            if ((Integer)this.posX.getValue() == 1) {
                drawX = (xBase - (float)width - 6.0f) / scaleFactor;
            }
            float drawY = drawYBase + (float)(i * (this.getFontHeight(fontToUse) + (Integer)this.spacing.getValue())) / scaleFactor;
            if ((Integer)this.backgroundAlpha.getValue() > 0 && fontToUse == null) {
                int bgColor = new Color(15, 15, 15, (Integer)this.backgroundAlpha.getValue()).getRGB();
                this.drawRoundedRect(drawX, drawY, (float)(width + 6) / scaleFactor, (float)(this.getFontHeight(fontToUse) + ((Boolean)this.shadow.getValue() != false ? 2 : 0)) / scaleFactor, (float)((Integer)this.cornerRadius.getValue()).intValue() / scaleFactor, bgColor);
            }
            if (fontToUse != null) {
                fontToUse.drawString(name, drawX * scaleFactor + 3.0f, drawY * scaleFactor, colorInt);
            } else if (((Boolean)this.multiLayerGlow.getValue()).booleanValue() && activationWeight > 0.1f) {
                this.drawMultiLayerText(name, drawX * scaleFactor + 3.0f, drawY * scaleFactor, finalColor, activationWeight);
            } else if (((Boolean)this.shadow.getValue()).booleanValue()) {
                HUD.mc.field_71466_p.func_175063_a(name, drawX * scaleFactor + 3.0f, drawY * scaleFactor, colorInt);
            } else {
                HUD.mc.field_71466_p.func_175065_a(name, drawX * scaleFactor + 3.0f, drawY * scaleFactor, colorInt, false);
            }
            if (suf != null) {
                float nx = drawX * scaleFactor + (float)(fontToUse != null ? fontToUse.getStringWidth(name) : HUD.mc.field_71466_p.func_78256_a(name)) + 6.0f;
                for (String s : suf) {
                    int suffixColor = -5592406;
                    if (fontToUse != null) {
                        fontToUse.drawString(s, nx, drawY * scaleFactor, suffixColor);
                    } else if (((Boolean)this.shadow.getValue()).booleanValue()) {
                        HUD.mc.field_71466_p.func_175063_a(s, nx, drawY * scaleFactor, suffixColor);
                    } else {
                        HUD.mc.field_71466_p.func_175065_a(s, nx, drawY * scaleFactor, suffixColor, false);
                    }
                    nx += (float)((fontToUse != null ? fontToUse.getStringWidth(s) : HUD.mc.field_71466_p.func_78256_a(s)) + 3);
                }
            }
            if (!((Boolean)this.showBar.getValue()).booleanValue()) continue;
            float barX = drawX + (float)((Integer)this.posX.getValue() == 0 ? -3 : width + 3) / scaleFactor;
            float barY = drawY;
            float barW = 1.5f / scaleFactor;
            float barH = (float)(this.getFontHeight(fontToUse) + ((Boolean)this.shadow.getValue() != false ? 2 : 0)) / scaleFactor;
            RenderUtil.drawRect(barX, barY, barX + barW, barY + barH, (Boolean)this.shadow.getValue() != false ? colorInt : colorInt & 0x7F7F7F | Integer.MIN_VALUE);
        }
        if (((Boolean)this.blinkTimer.getValue()).booleanValue() && (blinking = Myau.blinkManager.getBlinkingModule()) != BlinkModules.NONE && blinking != BlinkModules.AUTO_BLOCK && (packets = Myau.blinkManager.countMovement()) > 0L) {
            GlStateManager.func_179147_l();
            GlStateManager.func_179112_b((int)770, (int)771);
            String text = String.valueOf(packets);
            float centerX = (float)sr.func_78326_a() / 2.0f;
            float centerY = (float)sr.func_78328_b() * 0.6f;
            int color = new Color((Integer)this.gradientStart.getValue()).getRGB() & 0xFFFFFF | Integer.MIN_VALUE;
            HUD.mc.field_71466_p.func_175063_a(text, centerX - (float)HUD.mc.field_71466_p.func_78256_a(text) / 2.0f, centerY, color);
            GlStateManager.func_179084_k();
        }
        GlStateManager.func_179121_F();
    }

    public Color getColor() {
        return new Color((Integer)this.gradientStart.getValue());
    }

    public Color getColor(long offset) {
        return this.getColor();
    }
}
