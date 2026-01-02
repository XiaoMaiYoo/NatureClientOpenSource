package myau.util;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import myau.enums.ChatColors;
import myau.mixin.IAccessorEntityRenderer;
import myau.mixin.IAccessorMinecraft;
import myau.mixin.IAccessorRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class RenderUtil {
    private static Minecraft mc = Minecraft.func_71410_x();
    private static Frustum cameraFrustum = new Frustum();
    private static IntBuffer viewportBuffer = GLAllocation.func_74527_f((int)16);
    private static FloatBuffer modelViewBuffer = GLAllocation.func_74529_h((int)16);
    private static FloatBuffer projectionBuffer = GLAllocation.func_74529_h((int)16);
    private static FloatBuffer vectorBuffer = GLAllocation.func_74529_h((int)4);
    private static Map<Integer, EnchantmentData> enchantmentMap = new EnchantmentMap();

    private static ChatColors getColorForLevel(int currentLevel, int maxLevel) {
        if (currentLevel > maxLevel) {
            return ChatColors.LIGHT_PURPLE;
        }
        if (currentLevel == maxLevel) {
            return ChatColors.RED;
        }
        switch (currentLevel) {
            case 1: {
                return ChatColors.AQUA;
            }
            case 2: {
                return ChatColors.GREEN;
            }
            case 3: {
                return ChatColors.YELLOW;
            }
            case 4: {
                return ChatColors.GOLD;
            }
        }
        return ChatColors.GRAY;
    }

    public static void drawOutlinedString(String text, float x, float y) {
        String string2 = text.replaceAll("(?i)\u00c2\u00a7[\\da-f]", "");
        RenderUtil.mc.field_71466_p.func_175065_a(string2, x + 1.0f, y, 0, false);
        RenderUtil.mc.field_71466_p.func_175065_a(string2, x - 1.0f, y, 0, false);
        RenderUtil.mc.field_71466_p.func_175065_a(string2, x, y + 1.0f, 0, false);
        RenderUtil.mc.field_71466_p.func_175065_a(string2, x, y - 1.0f, 0, false);
        RenderUtil.mc.field_71466_p.func_175065_a(text, x, y, -1, false);
    }

    public static void renderEnchantmentText(ItemStack itemStack, float x, float y, float scale) {
        NBTTagList nBTTagList;
        NBTTagList nBTTagList2 = nBTTagList = itemStack.func_77973_b() == Items.field_151134_bR ? Items.field_151134_bR.func_92110_g(itemStack) : itemStack.func_77986_q();
        if (nBTTagList != null) {
            for (int i = 0; i < nBTTagList.func_74745_c(); ++i) {
                EnchantmentData enchantmentData = enchantmentMap.get(nBTTagList.func_150305_b(i).func_74762_e("id"));
                if (enchantmentData == null) continue;
                short s = nBTTagList.func_150305_b(i).func_74765_d("lvl");
                ChatColors chatColors = RenderUtil.getColorForLevel(s, enchantmentData.maxLevel);
                RenderUtil.drawOutlinedString(ChatColors.formatColor(String.format("&r%s%s%d&r", new Object[]{enchantmentData.shortName, chatColors, (int)s})), x * (1.0f / scale), (y + (float)i * 4.0f) * (1.0f / scale));
            }
        }
    }

    public static void renderItemInGUI(ItemStack itemStack, int x, int y) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179132_a((boolean)true);
        GlStateManager.func_179086_m((int)256);
        RenderHelper.func_74520_c();
        GL11.glDisable((int)2896);
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)1.0f, (float)1.0f, (float)-0.01f);
        RenderUtil.mc.func_175599_af().field_77023_b = -150.0f;
        mc.func_175599_af().func_180450_b(itemStack, x, y);
        mc.func_175599_af().func_175030_a(RenderUtil.mc.field_71466_p, itemStack, x, y);
        RenderUtil.mc.func_175599_af().field_77023_b = 0.0f;
        GlStateManager.func_179121_F();
        RenderHelper.func_74518_a();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
        GlStateManager.func_179098_w();
        GlStateManager.func_179121_F();
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)0.5f, (float)0.5f, (float)0.5f);
        GlStateManager.func_179097_i();
        RenderUtil.renderEnchantmentText(itemStack, x, y, 0.5f);
        GlStateManager.func_179126_j();
        GlStateManager.func_179152_a((float)2.0f, (float)2.0f, (float)2.0f);
        GlStateManager.func_179121_F();
    }

    public static void renderPotionEffect(PotionEffect potionEffect, int x, int y) {
        int n3 = Potion.field_76425_a[potionEffect.func_76456_a()].func_76392_e();
        GlStateManager.func_179131_c((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.func_179094_E();
        GlStateManager.func_179132_a((boolean)true);
        GlStateManager.func_179086_m((int)256);
        GlStateManager.func_179094_E();
        GlStateManager.func_179152_a((float)1.0f, (float)1.0f, (float)-0.01f);
        mc.func_110434_K().func_110577_a(new ResourceLocation("textures/gui/container/inventory.png"));
        Gui.func_146110_a((int)x, (int)y, (float)(n3 % 8 * 18), (float)(198 + n3 / 8 * 18), (int)18, (int)18, (float)256.0f, (float)256.0f);
        GlStateManager.func_179121_F();
        GlStateManager.func_179141_d();
        GlStateManager.func_179084_k();
        GlStateManager.func_179098_w();
        GlStateManager.func_179121_F();
    }

    public static void drawRect(float x1, float y1, float x2, float y2, int color) {
        if (color == 0) {
            return;
        }
        RenderUtil.setColor(color);
        GL11.glBegin((int)9);
        GL11.glVertex2f((float)x1, (float)y1);
        GL11.glVertex2f((float)x1, (float)y2);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glVertex2f((float)x2, (float)y1);
        GL11.glEnd();
        GlStateManager.func_179117_G();
    }

    public static void drawRect3D(float x1, float y1, float x2, float y2, int color) {
        if (color == 0) {
            return;
        }
        RenderUtil.setColor(color);
        GL11.glEnable((int)2881);
        GL11.glHint((int)3155, (int)4354);
        GL11.glBegin((int)9);
        for (int i = 0; i < 2; ++i) {
            GL11.glVertex2f((float)x1, (float)y1);
            GL11.glVertex2f((float)x1, (float)y2);
            GL11.glVertex2f((float)x2, (float)y2);
            GL11.glVertex2f((float)x2, (float)y1);
        }
        GL11.glEnd();
        GL11.glDisable((int)2881);
        GlStateManager.func_179117_G();
    }

    public static void drawOutlineRect(float x1, float y1, float x2, float y2, float lineWidth, int backgroundColor, int lineColor) {
        RenderUtil.drawRect(0.0f, 0.0f, x2, 27.0f, backgroundColor);
        if (lineColor == 0) {
            return;
        }
        RenderUtil.setColor(lineColor);
        GL11.glLineWidth((float)lineWidth);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glBegin((int)1);
        GL11.glVertex2f((float)x1, (float)y1);
        GL11.glVertex2f((float)x1, (float)y2);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glVertex2f((float)x2, (float)y1);
        GL11.glVertex2f((float)x1, (float)y1);
        GL11.glVertex2f((float)x2, (float)y1);
        GL11.glVertex2f((float)x1, (float)y2);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glEnd();
        GL11.glDisable((int)2848);
        GL11.glLineWidth((float)2.0f);
        GlStateManager.func_179117_G();
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float lineWidth, int color) {
        RenderUtil.setColor(color);
        GL11.glLineWidth((float)lineWidth);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glBegin((int)1);
        GL11.glVertex2f((float)x1, (float)y1);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glEnd();
        GL11.glDisable((int)2848);
        GL11.glLineWidth((float)2.0f);
        GlStateManager.func_179117_G();
    }

    public static void drawLine3D(Vec3 start, double endX, double endY, double endZ, float red, float green, float blue, float alpha, float lineWidth) {
        GlStateManager.func_179094_E();
        GlStateManager.func_179131_c((float)red, (float)green, (float)blue, (float)alpha);
        boolean bl = RenderUtil.mc.field_71474_y.field_74336_f;
        RenderUtil.mc.field_71474_y.field_74336_f = false;
        ((IAccessorEntityRenderer)RenderUtil.mc.field_71460_t).callSetupCameraTransform(((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c, 2);
        RenderUtil.mc.field_71474_y.field_74336_f = bl;
        GL11.glLineWidth((float)lineWidth);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glBegin((int)1);
        GL11.glVertex3d((double)start.field_72450_a, (double)start.field_72448_b, (double)start.field_72449_c);
        GL11.glVertex3d((double)(endX - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX()), (double)(endY - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY()), (double)(endZ - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()));
        GL11.glEnd();
        GL11.glDisable((int)2848);
        GL11.glLineWidth((float)2.0f);
        GlStateManager.func_179117_G();
        GlStateManager.func_179121_F();
    }

    public static void drawArrow(float centerX, float centerY, float angle, float length, float lineWidth, int color) {
        float f6 = angle + (float)Math.toRadians(45.0);
        float f7 = angle - (float)Math.toRadians(45.0);
        RenderUtil.setColor(color);
        GL11.glLineWidth((float)lineWidth);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glBegin((int)1);
        GL11.glVertex2f((float)centerX, (float)centerY);
        GL11.glVertex2f((float)(centerX + length * (float)Math.cos(f6)), (float)(centerY + length * (float)Math.sin(f6)));
        GL11.glVertex2f((float)centerX, (float)centerY);
        GL11.glVertex2f((float)(centerX + length * (float)Math.cos(f7)), (float)(centerY + length * (float)Math.sin(f7)));
        GL11.glEnd();
        GL11.glDisable((int)2848);
        GL11.glLineWidth((float)2.0f);
        GlStateManager.func_179117_G();
    }

    public static void drawTriangle(float centerX, float centerY, float angle, float length, int color) {
        float f5 = angle + (float)Math.toRadians(26.25);
        float f6 = angle - (float)Math.toRadians(26.25);
        RenderUtil.setColor(color);
        GL11.glEnable((int)2881);
        GL11.glHint((int)3155, (int)4354);
        GL11.glBegin((int)9);
        GL11.glVertex2f((float)centerX, (float)centerY);
        GL11.glVertex2f((float)(centerX + length * (float)Math.cos(f5)), (float)(centerY + length * (float)Math.sin(f5)));
        GL11.glVertex2f((float)(centerX + length * (float)Math.cos(f6)), (float)(centerY + length * (float)Math.sin(f6)));
        GL11.glEnd();
        GL11.glDisable((int)2881);
        GlStateManager.func_179117_G();
    }

    public static void drawFramebuffer(Framebuffer framebuffer) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        GlStateManager.func_179144_i((int)framebuffer.field_147617_g);
        GL11.glBegin((int)7);
        GL11.glTexCoord2d((double)0.0, (double)1.0);
        GL11.glVertex2d((double)0.0, (double)0.0);
        GL11.glTexCoord2d((double)0.0, (double)0.0);
        GL11.glVertex2d((double)0.0, (double)scaledResolution.func_78328_b());
        GL11.glTexCoord2d((double)1.0, (double)0.0);
        GL11.glVertex2d((double)scaledResolution.func_78326_a(), (double)scaledResolution.func_78328_b());
        GL11.glTexCoord2d((double)1.0, (double)1.0);
        GL11.glVertex2d((double)scaledResolution.func_78326_a(), (double)0.0);
        GL11.glEnd();
    }

    public static void drawCircle(double centerX, double centerY, double centerZ, double radius, int segments, int color) {
        RenderUtil.setColor(color);
        GL11.glLineWidth((float)3.0f);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glBegin((int)2);
        for (int i = 0; i <= segments; ++i) {
            double d5 = (double)i * (Math.PI * 2 / (double)segments);
            GL11.glVertex3d((double)(centerX + Math.cos(d5) * radius), (double)centerY, (double)(centerZ + Math.sin(d5) * radius));
        }
        GL11.glEnd();
        GL11.glDisable((int)2848);
        GL11.glLineWidth((float)2.0f);
        GlStateManager.func_179117_G();
    }

    public static void drawEntityCircle(Entity entity, double radius, int segments, int color) {
        double d2 = RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX();
        double d3 = RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY();
        double d4 = RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ();
        RenderUtil.drawCircle(d2, d3, d4, radius, segments, color);
    }

    public static void drawFilledBox(AxisAlignedBB axisAlignedBB, int red, int green, int blue) {
        Tessellator tessellator = Tessellator.func_178181_a();
        WorldRenderer worldRenderer = tessellator.func_178180_c();
        worldRenderer.func_181668_a(7, DefaultVertexFormats.field_181706_f);
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        worldRenderer.func_181662_b(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f).func_181669_b(red, green, blue, 63).func_181675_d();
        tessellator.func_78381_a();
    }

    public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, int red, int green, int blue, int alpha, float lineWidth) {
        GL11.glLineWidth((float)lineWidth);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        RenderGlobal.func_181563_a((AxisAlignedBB)axisAlignedBB, (int)red, (int)green, (int)blue, (int)alpha);
        GL11.glDisable((int)2848);
        GL11.glLineWidth((float)2.0f);
    }

    public static void drawEntityBox(Entity entity, int red, int green, int blue) {
        double d2 = RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        double d3 = RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        double d4 = RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        RenderUtil.drawFilledBox(entity.func_174813_aQ().func_72314_b((double)0.1f, (double)0.1f, (double)0.1f).func_72317_d(d2 - entity.field_70165_t, d3 - entity.field_70163_u, d4 - entity.field_70161_v).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()), red, green, blue);
    }

    public static void drawEntityBoundingBox(Entity entity, int red, int green, int blue, int alpha, float lineWidth, double expand) {
        double d2 = RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        double d3 = RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        double d4 = RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        RenderUtil.drawBoundingBox(entity.func_174813_aQ().func_72314_b(expand, expand, expand).func_72317_d(d2 - entity.field_70165_t, d3 - entity.field_70163_u, d4 - entity.field_70161_v).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()), red, green, blue, alpha, lineWidth);
    }

    public static void drawBlockBox(BlockPos blockPos, double height, int red, int green, int blue) {
        RenderUtil.drawFilledBox(new AxisAlignedBB((double)blockPos.func_177958_n(), (double)blockPos.func_177956_o(), (double)blockPos.func_177952_p(), (double)blockPos.func_177958_n() + 1.0, (double)blockPos.func_177956_o() + height, (double)blockPos.func_177952_p() + 1.0).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()), red, green, blue);
    }

    public static void drawBlockBoundingBox(BlockPos blockPos, double height, int red, int green, int blue, int alpha, float lineWidth) {
        RenderUtil.drawBoundingBox(new AxisAlignedBB((double)blockPos.func_177958_n(), (double)blockPos.func_177956_o(), (double)blockPos.func_177952_p(), (double)blockPos.func_177958_n() + 1.0, (double)blockPos.func_177956_o() + height, (double)blockPos.func_177952_p() + 1.0).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ()), red, green, blue, alpha, lineWidth);
    }

    public static void drawCornerESP(EntityPlayer entity, float red, float green, float blue) {
        float x = (float)(RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX());
        float y = (float)(RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY());
        float z = (float)(RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ());
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)x, (float)(y + entity.field_70131_O / 2.0f), (float)z);
        GlStateManager.func_179114_b((float)(-RenderUtil.mc.func_175598_ae().field_78735_i), (float)0.0f, (float)1.0f, (float)0.0f);
        GlStateManager.func_179152_a((float)-0.098f, (float)-0.098f, (float)0.098f);
        float width = (float)(26.6 * (double)entity.field_70130_N / 2.0);
        float height = 12.0f;
        GlStateManager.func_179124_c((float)red, (float)green, (float)blue);
        RenderUtil.draw3DRect(width, height - 1.0f, width - 4.0f, height);
        RenderUtil.draw3DRect(-width, height - 1.0f, -width + 4.0f, height);
        RenderUtil.draw3DRect(-width, height, -width + 1.0f, height - 4.0f);
        RenderUtil.draw3DRect(width, height, width - 1.0f, height - 4.0f);
        RenderUtil.draw3DRect(width, -height, width - 4.0f, -height + 1.0f);
        RenderUtil.draw3DRect(-width, -height, -width + 4.0f, -height + 1.0f);
        RenderUtil.draw3DRect(-width, -height + 1.0f, -width + 1.0f, -height + 4.0f);
        RenderUtil.draw3DRect(width, -height + 1.0f, width - 1.0f, -height + 4.0f);
        GlStateManager.func_179124_c((float)0.0f, (float)0.0f, (float)0.0f);
        RenderUtil.draw3DRect(width, height, width - 4.0f, height + 0.2f);
        RenderUtil.draw3DRect(-width, height, -width + 4.0f, height + 0.2f);
        RenderUtil.draw3DRect(-width - 0.2f, height + 0.2f, -width, height - 4.0f);
        RenderUtil.draw3DRect(width + 0.2f, height + 0.2f, width, height - 4.0f);
        RenderUtil.draw3DRect(width + 0.2f, -height, width - 4.0f, -height - 0.2f);
        RenderUtil.draw3DRect(-width - 0.2f, -height, -width + 4.0f, -height - 0.2f);
        RenderUtil.draw3DRect(-width - 0.2f, -height, -width, -height + 4.0f);
        RenderUtil.draw3DRect(width + 0.2f, -height, width, -height + 4.0f);
        GlStateManager.func_179131_c((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.func_179121_F();
    }

    public static void drawFake2DESP(EntityPlayer entity, float red, float green, float blue) {
        float x = (float)(RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX());
        float y = (float)(RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY());
        float z = (float)(RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c) - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ());
        GlStateManager.func_179094_E();
        GlStateManager.func_179109_b((float)x, (float)(y + entity.field_70131_O / 2.0f), (float)z);
        GlStateManager.func_179114_b((float)(-RenderUtil.mc.func_175598_ae().field_78735_i), (float)0.0f, (float)1.0f, (float)0.0f);
        GlStateManager.func_179152_a((float)-0.1f, (float)-0.1f, (float)0.1f);
        GlStateManager.func_179124_c((float)red, (float)green, (float)blue);
        float width = (float)(23.3 * (double)entity.field_70130_N / 2.0);
        float height = 12.0f;
        RenderUtil.draw3DRect(width, height, -width, height + 0.4f);
        RenderUtil.draw3DRect(width, -height, -width, -height + 0.4f);
        RenderUtil.draw3DRect(width, -height + 0.4f, width - 0.4f, height + 0.4f);
        RenderUtil.draw3DRect(-width, -height + 0.4f, -width + 0.4f, height + 0.4f);
        GlStateManager.func_179131_c((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GlStateManager.func_179121_F();
    }

    public static void draw3DRect(float x1, float y1, float x2, float y2) {
        GL11.glBegin((int)9);
        GL11.glVertex2f((float)x2, (float)y1);
        GL11.glVertex2f((float)x1, (float)y1);
        GL11.glVertex2f((float)x1, (float)y2);
        GL11.glVertex2f((float)x2, (float)y2);
        GL11.glEnd();
    }

    public static Vector4d projectToScreen(Entity entity, double screenScale) {
        double d3 = RenderUtil.lerpDouble(entity.field_70165_t, entity.field_70142_S, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        double d4 = RenderUtil.lerpDouble(entity.field_70163_u, entity.field_70137_T, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        double d5 = RenderUtil.lerpDouble(entity.field_70161_v, entity.field_70136_U, ((IAccessorMinecraft)RenderUtil.mc).getTimer().field_74281_c);
        AxisAlignedBB axisAlignedBB = entity.func_174813_aQ().func_72314_b((double)0.1f, (double)0.1f, (double)0.1f).func_72317_d(d3 - entity.field_70165_t, d4 - entity.field_70163_u, d5 - entity.field_70161_v);
        Vector4d vector4d = null;
        for (Vector3d vector3d : new Vector3d[]{new Vector3d(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c), new Vector3d(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c), new Vector3d(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72339_c), new Vector3d(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72339_c), new Vector3d(axisAlignedBB.field_72340_a, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f), new Vector3d(axisAlignedBB.field_72340_a, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f), new Vector3d(axisAlignedBB.field_72336_d, axisAlignedBB.field_72338_b, axisAlignedBB.field_72334_f), new Vector3d(axisAlignedBB.field_72336_d, axisAlignedBB.field_72337_e, axisAlignedBB.field_72334_f)}) {
            GL11.glGetFloat((int)2982, (FloatBuffer)modelViewBuffer);
            GL11.glGetFloat((int)2983, (FloatBuffer)projectionBuffer);
            GL11.glGetInteger((int)2978, (IntBuffer)viewportBuffer);
            if (!GLU.gluProject((float)((float)(vector3d.x - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX())), (float)((float)(vector3d.y - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY())), (float)((float)(vector3d.z - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ())), (FloatBuffer)modelViewBuffer, (FloatBuffer)projectionBuffer, (IntBuffer)viewportBuffer, (FloatBuffer)vectorBuffer)) continue;
            vector3d = new Vector3d((double)vectorBuffer.get(0) / screenScale, (double)((float)Display.getHeight() - vectorBuffer.get(1)) / screenScale, (double)vectorBuffer.get(2));
            if (!(vector3d.z >= 0.0) || !(vector3d.z < 1.0)) continue;
            if (vector4d == null) {
                vector4d = new Vector4d(vector3d.x, vector3d.y, vector3d.z, 0.0);
            }
            vector4d.x = Math.min(vector3d.x, vector4d.x);
            vector4d.y = Math.min(vector3d.y, vector4d.y);
            vector4d.z = Math.max(vector3d.x, vector4d.z);
            vector4d.w = Math.max(vector3d.y, vector4d.w);
        }
        return vector4d;
    }

    public static boolean isInViewFrustum(AxisAlignedBB axisAlignedBB, double expand) {
        cameraFrustum.func_78547_a(RenderUtil.mc.func_175606_aa().field_70165_t, RenderUtil.mc.func_175606_aa().field_70163_u, RenderUtil.mc.func_175606_aa().field_70161_v);
        return cameraFrustum.func_78546_a(axisAlignedBB.func_72314_b(expand, expand, expand));
    }

    public static void enableRenderState() {
        GlStateManager.func_179147_l();
        GlStateManager.func_179112_b((int)770, (int)771);
        GlStateManager.func_179090_x();
        GlStateManager.func_179129_p();
        GlStateManager.func_179118_c();
        GlStateManager.func_179097_i();
    }

    public static void disableRenderState() {
        GlStateManager.func_179126_j();
        GlStateManager.func_179141_d();
        GlStateManager.func_179089_o();
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
    }

    public static void setColor(int argb) {
        float f = (float)(argb >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(argb >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(argb >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(argb & 0xFF) / 255.0f;
        GlStateManager.func_179131_c((float)f2, (float)f3, (float)f4, (float)f);
    }

    public static float lerpFloat(float current, float previous, float t) {
        return previous + (current - previous) * t;
    }

    public static double lerpDouble(double current, double previous, double t) {
        return previous + (current - previous) * t;
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0f / textureWidth;
        float f1 = 1.0f / textureHeight;
        Tessellator tessellator = Tessellator.func_178181_a();
        WorldRenderer worldrenderer = tessellator.func_178180_c();
        worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldrenderer.func_181662_b((double)x, (double)(y + height), 0.0).func_181673_a((double)(u * f), (double)((v + height) * f1)).func_181675_d();
        worldrenderer.func_181662_b((double)(x + width), (double)(y + height), 0.0).func_181673_a((double)((u + width) * f), (double)((v + height) * f1)).func_181675_d();
        worldrenderer.func_181662_b((double)(x + width), (double)y, 0.0).func_181673_a((double)((u + width) * f), (double)(v * f1)).func_181675_d();
        worldrenderer.func_181662_b((double)x, (double)y, 0.0).func_181673_a((double)(u * f), (double)(v * f1)).func_181675_d();
        tessellator.func_78381_a();
    }

    static final class EnchantmentMap
    extends HashMap<Integer, EnchantmentData> {
        EnchantmentMap() {
            this.put(0, new EnchantmentData("Pr", 4));
            this.put(1, new EnchantmentData("Fp", 4));
            this.put(2, new EnchantmentData("Ff", 4));
            this.put(3, new EnchantmentData("Bp", 4));
            this.put(4, new EnchantmentData("Pp", 4));
            this.put(5, new EnchantmentData("Re", 3));
            this.put(6, new EnchantmentData("Aq", 1));
            this.put(7, new EnchantmentData("Th", 3));
            this.put(8, new EnchantmentData("Ds", 3));
            this.put(16, new EnchantmentData("Sh", 5));
            this.put(17, new EnchantmentData("Sm", 5));
            this.put(18, new EnchantmentData("BoA", 5));
            this.put(19, new EnchantmentData("Kb", 2));
            this.put(20, new EnchantmentData("Fa", 2));
            this.put(21, new EnchantmentData("Lo", 3));
            this.put(32, new EnchantmentData("Ef", 5));
            this.put(33, new EnchantmentData("St", 1));
            this.put(34, new EnchantmentData("Ub", 3));
            this.put(35, new EnchantmentData("Fo", 3));
            this.put(48, new EnchantmentData("Po", 5));
            this.put(49, new EnchantmentData("Pu", 2));
            this.put(50, new EnchantmentData("Fl", 1));
            this.put(51, new EnchantmentData("Inf", 1));
            this.put(61, new EnchantmentData("LoS", 3));
            this.put(62, new EnchantmentData("Lu", 3));
        }
    }

    public static final class EnchantmentData {
        public final String shortName;
        public final int maxLevel;

        public EnchantmentData(String shortName, int maxLevel) {
            this.shortName = shortName;
            this.maxLevel = maxLevel;
        }
    }
}
