package myau.util;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Iterator;
import myau.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

public class ItemUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final ArrayList<Integer> specialItems = new SpecialItems();

    public static boolean isNotSpecialItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        Item item = itemStack.func_77973_b();
        if (item instanceof ItemBlock) {
            return !ItemUtil.isContainerBlock((ItemBlock)item);
        }
        if (item instanceof ItemPotion) {
            return ((ItemPotion)item).func_77832_l(itemStack).stream().map(PotionEffect::func_76456_a).noneMatch(specialItems::contains);
        }
        if (item instanceof ItemEnderPearl) {
            return false;
        }
        if (item instanceof ItemFood && item != Items.field_151070_bp) {
            return false;
        }
        return item != Items.field_151156_bN;
    }

    public static boolean isBlock(ItemStack itemStack) {
        if (itemStack == null || itemStack.field_77994_a < 1) {
            return false;
        }
        Item item = itemStack.func_77973_b();
        if (item instanceof ItemBlock) {
            return ItemUtil.isContainerBlock((ItemBlock)item);
        }
        return false;
    }

    public static boolean isContainerBlock(ItemBlock itemBlock) {
        Block block = itemBlock.func_179223_d();
        if (BlockUtil.isInteractable(block)) {
            return false;
        }
        return BlockUtil.isSolid(block);
    }

    public static double getAttackBonus(ItemStack itemStack) {
        double attackBonus = 0.0;
        if (itemStack == null) {
            return 0.0;
        }
        Multimap multimap = itemStack.func_111283_C();
        for (String attributeName : multimap.keySet()) {
            if (!attributeName.equals("generic.attackDamage")) continue;
            Iterator iterator = multimap.get(attributeName).iterator();
            if (!iterator.hasNext()) break;
            attackBonus += ((AttributeModifier)iterator.next()).func_111164_d();
            break;
        }
        if (itemStack.func_77948_v()) {
            attackBonus = attackBonus + (double)EnchantmentHelper.func_77506_a((int)Enchantment.field_77334_n.field_77352_x, (ItemStack)itemStack) + (double)EnchantmentHelper.func_77506_a((int)Enchantment.field_180314_l.field_77352_x, (ItemStack)itemStack) * 1.25;
        }
        return attackBonus;
    }

    public static float getToolEfficiency(ItemStack itemStack) {
        int enchantLevel;
        float efficiency = 1.0f;
        if (itemStack != null && itemStack.func_77973_b() instanceof ItemTool && (efficiency = ((ItemTool)itemStack.func_77973_b()).func_150913_i().func_77998_b()) > 1.0f && (enchantLevel = EnchantmentHelper.func_77506_a((int)Enchantment.field_77349_p.field_77352_x, (ItemStack)itemStack)) > 0) {
            efficiency += (float)(enchantLevel * enchantLevel + 1);
        }
        return efficiency;
    }

    public static double getArmorProtection(ItemStack itemStack) {
        double protection = 0.0;
        if (itemStack != null && itemStack.func_77973_b() instanceof ItemArmor) {
            protection = 0.0 + (double)((ItemArmor)itemStack.func_77973_b()).field_77879_b;
            if (itemStack.func_77948_v()) {
                protection += (double)EnchantmentHelper.func_77506_a((int)Enchantment.field_180310_c.field_77352_x, (ItemStack)itemStack) * 0.25;
            }
        }
        return protection;
    }

    public static int findSwordInInventorySlot(int startSlot, boolean checkDurability) {
        int bestSlot = -1;
        double bestAttackBonus = 0.0;
        for (int i = 0; i < 36; ++i) {
            double attackBonus;
            int currentSlot = ((startSlot + i) % 36 + 36) % 36;
            ItemStack itemStack = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(currentSlot);
            if (itemStack == null || !(itemStack.func_77973_b() instanceof ItemSword) || checkDurability && itemStack.func_77951_h() && itemStack.func_77958_k() - itemStack.func_77952_i() < 30 || !((attackBonus = ItemUtil.getAttackBonus(itemStack)) > bestAttackBonus)) continue;
            bestSlot = currentSlot;
            bestAttackBonus = attackBonus;
        }
        return bestSlot;
    }

    public static int findInventorySlot(String toolClass, int startSlot, boolean checkDurability) {
        int bestSlot = -1;
        float bestEfficiency = 1.0f;
        for (int i = 0; i < 36; ++i) {
            float efficiency;
            int currentSlot = ((startSlot + i) % 36 + 36) % 36;
            ItemStack itemStack = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(currentSlot);
            if (itemStack == null || !(itemStack.func_77973_b() instanceof ItemTool) || !itemStack.func_77973_b().getToolClasses(itemStack).contains(toolClass) || checkDurability && itemStack.func_77951_h() && itemStack.func_77958_k() - itemStack.func_77952_i() < 30 || !((efficiency = ItemUtil.getToolEfficiency(itemStack)) > bestEfficiency)) continue;
            bestSlot = currentSlot;
            bestEfficiency = efficiency;
        }
        return bestSlot;
    }

    public static int findInventorySlot(int currentSlot, Block block) {
        ItemStack currentItem = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(currentSlot);
        int bestSlot = currentSlot;
        float bestStrength = currentItem != null ? currentItem.func_150997_a(block) : 1.0f;
        for (int i = 0; i < 9; ++i) {
            float strength;
            ItemStack itemStack = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (itemStack == null || !((strength = itemStack.func_150997_a(block)) > bestStrength)) continue;
            bestSlot = i;
            bestStrength = strength;
        }
        return bestSlot;
    }

    public static int findArmorInventorySlot(int armorType, boolean checkDurability) {
        int bestSlot = -1;
        double bestProtection = 0.0;
        for (int i = 0; i < 40; ++i) {
            double protection;
            ItemStack itemStack = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (itemStack == null || !(itemStack.func_77973_b() instanceof ItemArmor) || ((ItemArmor)itemStack.func_77973_b()).field_77881_a != armorType || checkDurability && itemStack.func_77951_h() && itemStack.func_77958_k() - itemStack.func_77952_i() < 30 || !((protection = ItemUtil.getArmorProtection(itemStack)) >= bestProtection)) continue;
            bestSlot = i;
            bestProtection = protection;
        }
        return bestSlot;
    }

    public static int findInventorySlot(int startSlot) {
        int bestSlot = -1;
        int maxStackSize = 0;
        for (int i = 0; i < 36; ++i) {
            int currentSlot = ((startSlot + i) % 36 + 36) % 36;
            ItemStack itemStack = ItemUtil.mc.field_71439_g.field_71071_by.func_70301_a(currentSlot);
            if (itemStack == null || !ItemUtil.isBlock(itemStack) || maxStackSize >= itemStack.field_77994_a) continue;
            bestSlot = currentSlot;
            maxStackSize = itemStack.field_77994_a;
        }
        return bestSlot;
    }

    public static boolean hasRawUnbreakingEnchant() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null) {
            return false;
        }
        if (itemStack.func_77942_o()) {
            long id;
            NBTTagCompound extra;
            NBTTagCompound tag = itemStack.func_77978_p();
            if (tag.func_74764_b("ExtraAttributes") && (extra = tag.func_74775_l("ExtraAttributes")).func_74764_b("UHCid") && ((id = extra.func_74763_f("UHCid")) == 50006L || id == 50009L)) {
                return true;
            }
            if (tag.func_74764_b("HideFlags") && itemStack.func_77973_b() instanceof ItemSpade && ((ItemSpade)itemStack.func_77973_b()).func_150913_i() == Item.ToolMaterial.EMERALD) {
                return true;
            }
        }
        if (itemStack.func_77973_b() instanceof ItemEnchantedBook) {
            return false;
        }
        if (EnchantmentHelper.func_82781_a((ItemStack)itemStack).containsKey(19)) {
            return true;
        }
        return itemStack.func_77973_b() instanceof ItemSword;
    }

    public static boolean isHoldingSword() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null) {
            return false;
        }
        return itemStack.func_77973_b() instanceof ItemSword;
    }

    public static boolean isHoldingTool() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null) {
            return false;
        }
        return itemStack.func_77973_b() instanceof ItemTool;
    }

    public static boolean isEating() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null) {
            return false;
        }
        if (ItemPotion.func_77831_g((int)itemStack.func_77973_b().getMetadata(itemStack))) {
            return false;
        }
        return itemStack.func_77975_n() == EnumAction.EAT || itemStack.func_77975_n() == EnumAction.DRINK;
    }

    public static boolean isUsingBow() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null) {
            return false;
        }
        return itemStack.func_77973_b() instanceof ItemBow;
    }

    public static boolean isHoldingNonEmpty() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null || itemStack.field_77994_a < 1) {
            return false;
        }
        return itemStack.func_77973_b() instanceof ItemBlock;
    }

    public static boolean isHoldingBlock() {
        return ItemUtil.isBlock(ItemUtil.mc.field_71439_g.func_70694_bm());
    }

    public static boolean hasHoldItem() {
        ItemStack itemStack = ItemUtil.mc.field_71439_g.func_70694_bm();
        if (itemStack == null || itemStack.field_77994_a < 1) {
            return false;
        }
        return itemStack.func_77973_b() instanceof ItemFireball;
    }

    static final class SpecialItems
    extends ArrayList<Integer> {
        SpecialItems() {
            this.add(1);
            this.add(3);
            this.add(5);
            this.add(6);
            this.add(8);
            this.add(10);
            this.add(11);
            this.add(12);
            this.add(14);
            this.add(21);
            this.add(22);
        }
    }
}
