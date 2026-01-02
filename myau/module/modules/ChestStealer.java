package myau.module.modules;

import java.util.ArrayList;
import java.util.Iterator;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.events.WindowClickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.ChatUtil;
import myau.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.RandomUtils;

public class ChestStealer
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int clickDelay = 0;
    private int oDelay = 0;
    private boolean inChest = false;
    private boolean warnedFull = false;
    public final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"Normal", "Instant"});
    public final IntProperty minDelay = new IntProperty("min-delay", 1, 0, 20);
    public final IntProperty maxDelay = new IntProperty("max-delay", 2, 0, 20);
    public final IntProperty openDelay = new IntProperty("open-delay", 1, 0, 20);
    public final BooleanProperty autoClose = new BooleanProperty("auto-close", false);
    public final BooleanProperty nameCheck = new BooleanProperty("name-check", true);
    public final BooleanProperty skipTrash = new BooleanProperty("skip-trash", true);

    private boolean isValidGameMode() {
        WorldSettings.GameType gameType = ChestStealer.mc.field_71442_b.func_178889_l();
        return gameType == WorldSettings.GameType.SURVIVAL || gameType == WorldSettings.GameType.ADVENTURE;
    }

    private void shiftClick(int windowId, int slotId) {
        ChestStealer.mc.field_71442_b.func_78753_a(windowId, slotId, 0, 1, (EntityPlayer)ChestStealer.mc.field_71439_g);
    }

    public ChestStealer() {
        super("ChestStealer", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        String name;
        if (event.getType() != EventType.PRE) {
            return;
        }
        if (this.clickDelay > 0) {
            --this.clickDelay;
        }
        if (this.oDelay > 0) {
            --this.oDelay;
        }
        if (!(ChestStealer.mc.field_71462_r instanceof GuiChest)) {
            this.inChest = false;
            return;
        }
        Container container = ((GuiChest)ChestStealer.mc.field_71462_r).field_147002_h;
        if (!(container instanceof ContainerChest)) {
            this.inChest = false;
            return;
        }
        if (!this.inChest) {
            this.inChest = true;
            this.warnedFull = false;
            this.oDelay = (Integer)this.openDelay.getValue() + 1;
        }
        if (this.oDelay > 0 || !this.isEnabled() || !this.isValidGameMode()) {
            return;
        }
        IInventory inventory = ((ContainerChest)container).func_85151_d();
        if (((Boolean)this.nameCheck.getValue()).booleanValue() && !(name = inventory.func_70005_c_()).equals(I18n.func_135052_a((String)"container.chest", (Object[])new Object[0])) && !name.equals(I18n.func_135052_a((String)"container.chestDouble", (Object[])new Object[0]))) {
            return;
        }
        if (ChestStealer.mc.field_71439_g.field_71071_by.func_70447_i() == -1) {
            if (!this.warnedFull) {
                ChatUtil.sendFormatted(String.format("%s%s: &cYour inventory is full!&r", Myau.clientName, this.getName()));
                this.warnedFull = true;
            }
            if (((Boolean)this.autoClose.getValue()).booleanValue()) {
                ChestStealer.mc.field_71439_g.func_71053_j();
            }
            return;
        }
        if ((Integer)this.mode.getValue() == 1) {
            ArrayList<Integer> itemsToSteal = new ArrayList<Integer>();
            if (((Boolean)this.skipTrash.getValue()).booleanValue()) {
                String[] toolTypes;
                double currentDamage;
                int bestSword = -1;
                double bestDamage = 0.0;
                for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                    ItemStack stack;
                    if (!container.func_75139_a(i).func_75216_d() || !((stack = container.func_75139_a(i).func_75211_c()).func_77973_b() instanceof ItemSword)) continue;
                    double damage = ItemUtil.getAttackBonus(stack);
                    if (bestSword != -1 && !(damage > bestDamage)) continue;
                    bestSword = i;
                    bestDamage = damage;
                }
                int currentSwordSlot = ItemUtil.findSwordInInventorySlot(0, true);
                double d = currentDamage = currentSwordSlot != -1 ? ItemUtil.getAttackBonus(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(currentSwordSlot)) : 0.0;
                if (bestDamage > currentDamage && bestSword != -1) {
                    itemsToSteal.add(bestSword);
                }
                for (int armorType = 0; armorType < 4; ++armorType) {
                    double currentProt;
                    int bestArmor = -1;
                    double bestProt = 0.0;
                    for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                        ItemStack stack;
                        if (!container.func_75139_a(i).func_75216_d() || !((stack = container.func_75139_a(i).func_75211_c()).func_77973_b() instanceof ItemArmor)) continue;
                        ItemArmor armor = (ItemArmor)stack.func_77973_b();
                        if (armor.field_77881_a != armorType) continue;
                        double prot = ItemUtil.getArmorProtection(stack);
                        if (bestArmor != -1 && !(prot > bestProt)) continue;
                        bestArmor = i;
                        bestProt = prot;
                    }
                    if (bestArmor == -1) continue;
                    int currentArmorSlot = ItemUtil.findArmorInventorySlot(armorType, true);
                    double d2 = currentProt = currentArmorSlot != -1 ? ItemUtil.getArmorProtection(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(currentArmorSlot)) : 0.0;
                    if (!(bestProt > currentProt)) continue;
                    itemsToSteal.add(bestArmor);
                }
                for (String type : toolTypes = new String[]{"pickaxe", "shovel", "axe"}) {
                    float currentEff;
                    int bestTool = -1;
                    float bestEff = 1.0f;
                    for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                        if (!container.func_75139_a(i).func_75216_d()) continue;
                        ItemStack stack = container.func_75139_a(i).func_75211_c();
                        if (!(type.equals("pickaxe") && stack.func_77973_b() instanceof ItemPickaxe || type.equals("shovel") && stack.func_77973_b() instanceof ItemSpade) && (!type.equals("axe") || !(stack.func_77973_b() instanceof ItemAxe))) continue;
                        float eff = ItemUtil.getToolEfficiency(stack);
                        if (bestTool != -1 && !(eff > bestEff)) continue;
                        bestTool = i;
                        bestEff = eff;
                    }
                    if (bestTool == -1) continue;
                    int currentToolSlot = ItemUtil.findInventorySlot(type, 0, true);
                    float f = currentEff = currentToolSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(currentToolSlot)) : 1.0f;
                    if (!(bestEff > currentEff)) continue;
                    itemsToSteal.add(bestTool);
                }
                for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                    ItemStack stack;
                    if (!container.func_75139_a(i).func_75216_d() || !((stack = container.func_75139_a(i).func_75211_c()).func_77973_b() instanceof ItemSnowball) && !(stack.func_77973_b() instanceof ItemEgg)) continue;
                    itemsToSteal.add(i);
                }
            }
            for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                if (!container.func_75139_a(i).func_75216_d()) continue;
                ItemStack stack = container.func_75139_a(i).func_75211_c();
                if (((Boolean)this.skipTrash.getValue()).booleanValue() && ItemUtil.isNotSpecialItem(stack) || itemsToSteal.contains(i)) continue;
                itemsToSteal.add(i);
            }
            Iterator i = itemsToSteal.iterator();
            while (i.hasNext()) {
                int slot = (Integer)i.next();
                this.shiftClick(container.field_75152_c, slot);
            }
            ChestStealer.mc.field_71439_g.func_71053_j();
            return;
        }
        if (this.clickDelay > 0) {
            return;
        }
        if (((Boolean)this.skipTrash.getValue()).booleanValue()) {
            float efficiency;
            float shovelEfficiency;
            float pickaxeEfficiency;
            double damage;
            int bestSword = -1;
            double bestDamage = 0.0;
            int[] bestArmorSlots = new int[]{-1, -1, -1, -1};
            double[] bestArmorProtection = new double[]{0.0, 0.0, 0.0, 0.0};
            int bestPickaxeSlot = -1;
            float bestPickaxeEfficiency = 1.0f;
            int bestShovelSlot = -1;
            float bestShovelEfficiency = 1.0f;
            int bestAxeSlot = -1;
            float bestAxeEfficiency = 1.0f;
            for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                if (!container.func_75139_a(i).func_75216_d()) continue;
                ItemStack stack = container.func_75139_a(i).func_75211_c();
                Item item = stack.func_77973_b();
                if (item instanceof ItemSword) {
                    double damage2 = ItemUtil.getAttackBonus(stack);
                    if (bestSword != -1 && !(damage2 > bestDamage)) continue;
                    bestSword = i;
                    bestDamage = damage2;
                    continue;
                }
                if (item instanceof ItemArmor) {
                    int armorType = ((ItemArmor)item).field_77881_a;
                    double protectionLevel = ItemUtil.getArmorProtection(stack);
                    if (bestArmorSlots[armorType] != -1 && !(protectionLevel > bestArmorProtection[armorType])) continue;
                    bestArmorSlots[armorType] = i;
                    bestArmorProtection[armorType] = protectionLevel;
                    continue;
                }
                if (item instanceof ItemPickaxe) {
                    float efficiency2 = ItemUtil.getToolEfficiency(stack);
                    if (bestPickaxeSlot != -1 && !(efficiency2 > bestPickaxeEfficiency)) continue;
                    bestPickaxeSlot = i;
                    bestPickaxeEfficiency = efficiency2;
                    continue;
                }
                if (item instanceof ItemSpade) {
                    float efficiency3 = ItemUtil.getToolEfficiency(stack);
                    if (bestShovelSlot != -1 && !(efficiency3 > bestShovelEfficiency)) continue;
                    bestShovelSlot = i;
                    bestShovelEfficiency = efficiency3;
                    continue;
                }
                if (!(item instanceof ItemAxe)) continue;
                float efficiency4 = ItemUtil.getToolEfficiency(stack);
                if (bestAxeSlot != -1 && !(efficiency4 > bestAxeEfficiency)) continue;
                bestAxeSlot = i;
                bestAxeEfficiency = efficiency4;
            }
            int swordInInventorySlot = ItemUtil.findSwordInInventorySlot(0, true);
            double d = damage = swordInInventorySlot != -1 ? ItemUtil.getAttackBonus(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(swordInInventorySlot)) : 0.0;
            if (bestDamage > damage) {
                this.shiftClick(container.field_75152_c, bestSword);
                return;
            }
            for (int i = 0; i < 4; ++i) {
                double protectionLevel;
                int slot = ItemUtil.findArmorInventorySlot(i, true);
                double d3 = protectionLevel = slot != -1 ? ItemUtil.getArmorProtection(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(slot)) : 0.0;
                if (!(bestArmorProtection[i] > protectionLevel)) continue;
                this.shiftClick(container.field_75152_c, bestArmorSlots[i]);
                return;
            }
            int pickaxeSlot = ItemUtil.findInventorySlot("pickaxe", 0, true);
            float f = pickaxeEfficiency = pickaxeSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(pickaxeSlot)) : 1.0f;
            if (bestPickaxeEfficiency > pickaxeEfficiency) {
                this.shiftClick(container.field_75152_c, bestPickaxeSlot);
                return;
            }
            int shovelSlot = ItemUtil.findInventorySlot("shovel", 0, true);
            float f2 = shovelEfficiency = shovelSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(shovelSlot)) : 1.0f;
            if (bestShovelEfficiency > shovelEfficiency) {
                this.shiftClick(container.field_75152_c, bestShovelSlot);
                return;
            }
            int axeSlot = ItemUtil.findInventorySlot("axe", 0, true);
            float f3 = efficiency = axeSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(axeSlot)) : 1.0f;
            if (bestAxeEfficiency > efficiency) {
                this.shiftClick(container.field_75152_c, bestAxeSlot);
                return;
            }
        }
        for (int i = 0; i < inventory.func_70302_i_(); ++i) {
            if (!container.func_75139_a(i).func_75216_d()) continue;
            ItemStack stack = container.func_75139_a(i).func_75211_c();
            if (((Boolean)this.skipTrash.getValue()).booleanValue() && ItemUtil.isNotSpecialItem(stack)) continue;
            this.shiftClick(container.field_75152_c, i);
            this.clickDelay = RandomUtils.nextInt((Integer)this.minDelay.getValue() + 1, (Integer)this.maxDelay.getValue() + 2);
            return;
        }
        if (((Boolean)this.autoClose.getValue()).booleanValue()) {
            ChestStealer.mc.field_71439_g.func_71053_j();
        }
    }

    @EventTarget
    public void onWindowClick(WindowClickEvent event) {
        if ((Integer)this.mode.getValue() != 0) {
            return;
        }
        this.clickDelay = RandomUtils.nextInt((Integer)this.minDelay.getValue() + 1, (Integer)this.maxDelay.getValue() + 2);
    }

    @Override
    public void verifyValue(String mode) {
        switch (mode) {
            case "min-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.maxDelay.setValue(this.minDelay.getValue());
                break;
            }
            case "max-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.minDelay.setValue(this.maxDelay.getValue());
            }
        }
    }

    public boolean isWorking() {
        return ChestStealer.mc.field_71462_r instanceof GuiChest && this.isEnabled();
    }
}
