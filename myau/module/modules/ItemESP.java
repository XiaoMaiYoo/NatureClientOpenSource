package myau.module.modules;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.PercentProperty;
import myau.util.RenderUtil;
import myau.util.TeamUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

public class ItemESP
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final PercentProperty opacity = new PercentProperty("opacity", 25);
    public final BooleanProperty outline = new BooleanProperty("outline", false);
    public final BooleanProperty itemCount = new BooleanProperty("item-count", true);
    public final BooleanProperty autoScale = new BooleanProperty("auto-scale", true);
    public final BooleanProperty emeralds = new BooleanProperty("emeralds", true);
    public final BooleanProperty diamonds = new BooleanProperty("diamonds", true);
    public final BooleanProperty goldd = new BooleanProperty("gold", true);
    public final BooleanProperty iron = new BooleanProperty("iron", true);

    private boolean shouldHighlightItem(int itemId) {
        return (Boolean)this.emeralds.getValue() != false && this.isEmeraldItem(itemId) || (Boolean)this.diamonds.getValue() != false && this.isDiamondItem(itemId) || (Boolean)this.goldd.getValue() != false && this.isGoldItem(itemId) || (Boolean)this.iron.getValue() != false && this.isIronItem(itemId);
    }

    private boolean isEmeraldItem(int itemId) {
        Item item = Item.func_150899_d((int)itemId);
        Block block = Block.func_149634_a((Item)item);
        return item == Items.field_151166_bC || block == Blocks.field_150475_bE || block == Blocks.field_150412_bA;
    }

    private boolean isDiamondItem(int itemId) {
        Item item = Item.func_150899_d((int)itemId);
        Block block = Block.func_149634_a((Item)item);
        return item == Items.field_151045_i || item == Items.field_151048_u || item == Items.field_151046_w || item == Items.field_151047_v || item == Items.field_151056_x || item == Items.field_151012_L || item == Items.field_151161_ac || item == Items.field_151163_ad || item == Items.field_151173_ae || item == Items.field_151175_af || block == Blocks.field_150484_ah || block == Blocks.field_150482_ag;
    }

    private boolean isGoldItem(int itemId) {
        Item item = Item.func_150899_d((int)itemId);
        Block block = Block.func_149634_a((Item)item);
        return item == Items.field_151043_k || item == Items.field_151074_bl || item == Items.field_151153_ao || block == Blocks.field_150340_R || block == Blocks.field_150352_o;
    }

    private boolean isIronItem(int itemId) {
        Item item = Item.func_150899_d((int)itemId);
        Block block = Block.func_149634_a((Item)item);
        return item == Items.field_151042_j || block == Blocks.field_150339_S || block == Blocks.field_150366_p;
    }

    private Color getItemColor(int itemId) {
        if (this.isEmeraldItem(itemId)) {
            return new Color(ChatColors.GREEN.toAwtColor());
        }
        if (this.isDiamondItem(itemId)) {
            return new Color(ChatColors.AQUA.toAwtColor());
        }
        if (this.isGoldItem(itemId)) {
            return new Color(ChatColors.YELLOW.toAwtColor());
        }
        return this.isIronItem(itemId) ? new Color(ChatColors.WHITE.toAwtColor()) : new Color(ChatColors.GRAY.toAwtColor());
    }

    private int getItemPriority(int itemId) {
        if (this.isEmeraldItem(itemId)) {
            return 4;
        }
        if (this.isDiamondItem(itemId)) {
            return 3;
        }
        if (this.isGoldItem(itemId)) {
            return 2;
        }
        return this.isIronItem(itemId) ? 1 : 0;
    }

    public ItemESP() {
        super("ItemESP", false);
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled()) {
            LinkedHashMap<ItemData, Integer> itemMap = new LinkedHashMap<ItemData, Integer>();
            for (Entity entity : TeamUtil.getLoadedEntitiesSorted()) {
                int itemId;
                if (entity.field_70173_aa < 3 || !entity.field_70158_ak && !RenderUtil.isInViewFrustum(entity.func_174813_aQ(), 0.125) || !(entity instanceof EntityItem)) continue;
                EntityItem entityItem = (EntityItem)entity;
                ItemStack stack = entityItem.func_92059_d();
                if (stack.field_77994_a <= 0 || !this.shouldHighlightItem(itemId = Item.func_150891_b((Item)stack.func_77973_b()))) continue;
                double x = RenderUtil.lerpDouble(entityItem.field_70165_t, entityItem.field_70142_S, event.getPartialTicks());
                double y = RenderUtil.lerpDouble(entityItem.field_70163_u, entityItem.field_70137_T, event.getPartialTicks());
                double z = RenderUtil.lerpDouble(entityItem.field_70161_v, entityItem.field_70136_U, event.getPartialTicks());
                ItemData data = new ItemData(itemId, x, y, z);
                Integer id = (Integer)itemMap.get(data);
                itemMap.put(new ItemData(itemId, x, y, z), stack.field_77994_a + (id == null ? 0 : id));
            }
            for (Map.Entry itemEntry : itemMap.entrySet().stream().sorted((entry1, entry2) -> {
                int o = this.getItemPriority(((ItemData)entry1.getKey()).itemId);
                int o2 = this.getItemPriority(((ItemData)entry2.getKey()).itemId);
                return Integer.compare(o, o2);
            }).collect(Collectors.toList())) {
                Color itemColor = this.getItemColor(((ItemData)itemEntry.getKey()).itemId);
                double x = ((ItemData)itemEntry.getKey()).x - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX();
                double y = ((ItemData)itemEntry.getKey()).y - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY();
                double z = ((ItemData)itemEntry.getKey()).z - ((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ();
                double distance = mc.func_175606_aa().func_70011_f(((ItemData)itemEntry.getKey()).x, ((ItemData)itemEntry.getKey()).y, ((ItemData)itemEntry.getKey()).z);
                double scale = 0.5 + 0.375 * ((Math.max(6.0, (Boolean)this.autoScale.getValue() != false ? distance : 6.0) - 6.0) / 28.0);
                AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x - scale * 0.5, y, z - scale * 0.5, x + scale * 0.5, y + scale, z + scale * 0.5);
                RenderUtil.enableRenderState();
                if ((Integer)this.opacity.getValue() > 0) {
                    RenderUtil.drawFilledBox(axisAlignedBB, itemColor.getRed(), itemColor.getGreen(), itemColor.getBlue());
                    GlStateManager.func_179117_G();
                }
                if (((Boolean)this.outline.getValue()).booleanValue()) {
                    RenderUtil.drawBoundingBox(axisAlignedBB, itemColor.getRed(), itemColor.getGreen(), itemColor.getBlue(), 255, 1.5f);
                    GlStateManager.func_179117_G();
                }
                RenderUtil.disableRenderState();
                if (!((Boolean)this.itemCount.getValue()).booleanValue()) continue;
                GlStateManager.func_179094_E();
                GlStateManager.func_179137_b((double)x, (double)(y + scale * 0.5), (double)z);
                GlStateManager.func_179114_b((float)(ItemESP.mc.func_175598_ae().field_78735_i * -1.0f), (float)0.0f, (float)1.0f, (float)0.0f);
                float flip = ItemESP.mc.field_71474_y.field_74320_O == 2 ? -1.0f : 1.0f;
                GlStateManager.func_179114_b((float)ItemESP.mc.func_175598_ae().field_78732_j, (float)flip, (float)0.0f, (float)0.0f);
                double fontScale = -0.04375 - 0.0328125 * ((Math.max(6.0, (Boolean)this.autoScale.getValue() != false ? distance : 6.0) - 6.0) / 28.0);
                GlStateManager.func_179139_a((double)fontScale, (double)fontScale, (double)1.0);
                GlStateManager.func_179097_i();
                String countText = String.format("%d", itemEntry.getValue());
                RenderUtil.drawOutlinedString(countText, ((float)ItemESP.mc.field_71466_p.func_78256_a(countText) / 2.0f - 0.5f) * -1.0f, ((float)(ItemESP.mc.field_71466_p.field_78288_b / 2) - 0.5f) * -1.0f);
                GlStateManager.func_179126_j();
                GlStateManager.func_179117_G();
                GlStateManager.func_179121_F();
            }
        }
    }

    public static class ItemData {
        private final int hashCode;
        public final int itemId;
        public final double x;
        public final double y;
        public final double z;

        public ItemData(int id, double x, double y, double z) {
            this.itemId = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.hashCode = Objects.hash(id, (int)x, (int)y, (int)z);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object != null && this.getClass() == object.getClass()) {
                ItemData itemData = (ItemData)object;
                return this.itemId == itemData.itemId && (int)this.x == (int)itemData.x && (int)this.y == (int)itemData.y && (int)this.z == (int)itemData.z;
            }
            return false;
        }

        public int hashCode() {
            return this.hashCode;
        }
    }
}
