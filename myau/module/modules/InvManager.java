package myau.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.events.WindowClickEvent;
import myau.module.Module;
import myau.module.modules.ChestStealer;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.ItemUtil;
import myau.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.RandomUtils;

public class InvManager
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int actionDelay = 0;
    private int oDelay = 0;
    private boolean inventoryOpen = false;
    private boolean serverOpen = false;
    private boolean needsInventoryClose = false;
    public final IntProperty minDelay = new IntProperty("min-delay", 1, 0, 20);
    public final IntProperty maxDelay = new IntProperty("max-delay", 2, 0, 20);
    public final IntProperty openDelay = new IntProperty("open-delay", 1, 0, 20);
    public final BooleanProperty silent = new BooleanProperty("silent", true);
    public final BooleanProperty autoArmor = new BooleanProperty("auto-armor", true);
    public final BooleanProperty dropTrash = new BooleanProperty("drop-trash", false);
    public final IntProperty swordSlot = new IntProperty("sword-slot", 1, 0, 9);
    public final IntProperty pickaxeSlot = new IntProperty("pickaxe-slot", 3, 0, 9);
    public final IntProperty shovelSlot = new IntProperty("shovel-slot", 4, 0, 9);
    public final IntProperty axeSlot = new IntProperty("axe-slot", 5, 0, 9);
    public final IntProperty blocksSlot = new IntProperty("blocks-slot", 2, 0, 9);
    public final IntProperty blocks = new IntProperty("blocks", 128, 64, 2304);

    private boolean isProtectedItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        Item item = stack.func_77973_b();
        return item == Items.field_151126_ay || item == Items.field_151110_aK || item == Items.field_151079_bi || item == Items.field_151062_by || item == Items.field_151031_f || item == Items.field_151032_g || item == Items.field_151068_bn || item == Items.field_151034_e || item == Items.field_151168_bH || item == Items.field_151082_bd || item == Items.field_151025_P || item == Items.field_151172_bF || item == Items.field_151076_bf || item == Items.field_151083_be || item == Items.field_151077_bg || item == Items.field_179566_aV || item == Items.field_151157_am || item == Items.field_179559_bp || item == Items.field_151106_aX || item == Items.field_151115_aP || item == Items.field_151153_ao || item == Items.field_151150_bK || item == Items.field_151127_ba || item == Items.field_151009_A || item == Items.field_179561_bm || item == Items.field_151147_al || item == Items.field_151174_bG || item == Items.field_151158_bO || item == Items.field_179558_bo || item == Items.field_179560_bq || item == Items.field_151078_bh || item == Items.field_151070_bp || item == Items.field_151112_aM || item == Items.field_151033_d || item == Items.field_151097_aZ || item == Items.field_151131_as || item == Items.field_151129_at || item == Items.field_151117_aB || item == Items.field_151061_bv || item == Items.field_151111_aL || item == Items.field_151113_aN || item == Items.field_151058_ca || item == Items.field_151057_cb || item == Items.field_151141_av;
    }

    private boolean isValidGameMode() {
        WorldSettings.GameType gameType = InvManager.mc.field_71442_b.func_178889_l();
        return gameType == WorldSettings.GameType.SURVIVAL || gameType == WorldSettings.GameType.ADVENTURE;
    }

    private int convertSlotIndex(int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }
        return slot <= 8 ? slot + 36 : slot;
    }

    private void clickSlot(int windowId, int slotId, int mouseButtonClicked, int mode) {
        InvManager.mc.field_71442_b.func_78753_a(windowId, slotId, mouseButtonClicked, mode, (EntityPlayer)InvManager.mc.field_71439_g);
    }

    private int getStackSize(int slot) {
        if (slot == -1) {
            return 0;
        }
        ItemStack stack = InvManager.mc.field_71439_g.field_71071_by.func_70301_a(slot);
        return stack != null ? stack.field_77994_a : 0;
    }

    public InvManager() {
        super("InvManager", false);
    }

    @Override
    public void onDisabled() {
        if (((Boolean)this.silent.getValue()).booleanValue() && this.serverOpen) {
            PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
            this.serverOpen = false;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        boolean isInventoryOpen;
        if (event.getType() != EventType.PRE) {
            return;
        }
        if (((Boolean)this.silent.getValue()).booleanValue()) {
            Module chestStealerModule = Myau.moduleManager.modules.get(ChestStealer.class);
            if (chestStealerModule != null && chestStealerModule.isEnabled() && chestStealerModule instanceof ChestStealer && ((ChestStealer)chestStealerModule).isWorking()) {
                return;
            }
            if (!this.serverOpen) {
                if (this.needsInventoryClose) {
                    this.needsInventoryClose = false;
                    return;
                }
                PacketUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                this.serverOpen = true;
                this.needsInventoryClose = true;
                this.oDelay = (Integer)this.openDelay.getValue() + 1;
                return;
            }
        }
        if (this.actionDelay > 0) {
            --this.actionDelay;
        }
        if (this.oDelay > 0) {
            --this.oDelay;
        }
        if (((Boolean)this.silent.getValue()).booleanValue()) {
            isInventoryOpen = this.serverOpen;
        } else if (!(InvManager.mc.field_71462_r instanceof GuiInventory) || !(((GuiInventory)InvManager.mc.field_71462_r).field_147002_h instanceof ContainerPlayer)) {
            this.inventoryOpen = false;
            isInventoryOpen = false;
        } else {
            if (!this.inventoryOpen) {
                this.inventoryOpen = true;
                this.oDelay = (Integer)this.openDelay.getValue() + 1;
            }
            isInventoryOpen = true;
        }
        if (!isInventoryOpen || this.oDelay > 0 || this.actionDelay > 0) {
            return;
        }
        if (this.isEnabled() && this.isValidGameMode()) {
            int slot;
            ArrayList<Integer> equippedArmorSlots = new ArrayList<Integer>(Arrays.asList(-1, -1, -1, -1));
            ArrayList<Integer> inventoryArmorSlots = new ArrayList<Integer>(Arrays.asList(-1, -1, -1, -1));
            for (int i = 0; i < 4; ++i) {
                equippedArmorSlots.set(i, ItemUtil.findArmorInventorySlot(i, true));
                inventoryArmorSlots.set(i, ItemUtil.findArmorInventorySlot(i, false));
            }
            int preferredSwordHotbarSlot = (Integer)this.swordSlot.getValue() - 1;
            int equippedSwordSlot = ItemUtil.findSwordInInventorySlot(preferredSwordHotbarSlot, true);
            int inventorySwordSlot = ItemUtil.findSwordInInventorySlot(preferredSwordHotbarSlot, false);
            int preferredPickaxeHotbarSlot = (Integer)this.pickaxeSlot.getValue() - 1;
            int equippedPickaxeSlot = ItemUtil.findInventorySlot("pickaxe", preferredPickaxeHotbarSlot, true);
            int inventoryPickaxeSlot = ItemUtil.findInventorySlot("pickaxe", preferredPickaxeHotbarSlot, false);
            int preferredShovelHotbarSlot = (Integer)this.shovelSlot.getValue() - 1;
            int equippedShovelSlot = ItemUtil.findInventorySlot("shovel", preferredShovelHotbarSlot, true);
            int inventoryShovelSlot = ItemUtil.findInventorySlot("shovel", preferredShovelHotbarSlot, false);
            int preferredAxeHotbarSlot = (Integer)this.axeSlot.getValue() - 1;
            int equippedAxeSlot = ItemUtil.findInventorySlot("axe", preferredAxeHotbarSlot, true);
            int inventoryAxeSlot = ItemUtil.findInventorySlot("axe", preferredAxeHotbarSlot, false);
            int preferredBlocksHotbarSlot = (Integer)this.blocksSlot.getValue() - 1;
            int inventoryBlocksSlot = ItemUtil.findInventorySlot(preferredBlocksHotbarSlot);
            if (((Boolean)this.autoArmor.getValue()).booleanValue()) {
                for (int i = 0; i < 4; ++i) {
                    int playerArmorSlot;
                    int equippedSlot = equippedArmorSlots.get(i);
                    int inventorySlot = inventoryArmorSlots.get(i);
                    if (equippedSlot == -1 && inventorySlot == -1 || equippedSlot == (playerArmorSlot = 39 - i) || inventorySlot == playerArmorSlot) continue;
                    if (InvManager.mc.field_71439_g.field_71071_by.func_70301_a(playerArmorSlot) != null) {
                        if (InvManager.mc.field_71439_g.field_71071_by.func_70447_i() != -1) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(playerArmorSlot), 0, 1);
                        } else {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(playerArmorSlot), 1, 4);
                        }
                    } else {
                        int armorToEquipSlot = equippedSlot != -1 ? equippedSlot : inventorySlot;
                        this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(armorToEquipSlot), 0, 1);
                    }
                    if (((Boolean)this.silent.getValue()).booleanValue()) {
                        PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                        this.serverOpen = false;
                    }
                    return;
                }
            }
            LinkedHashSet<Integer> usedHotbarSlots = new LinkedHashSet<Integer>();
            if (preferredSwordHotbarSlot >= 0 && preferredSwordHotbarSlot <= 8 && (equippedSwordSlot != -1 || inventorySwordSlot != -1)) {
                usedHotbarSlots.add(preferredSwordHotbarSlot);
                if (equippedSwordSlot != preferredSwordHotbarSlot && inventorySwordSlot != preferredSwordHotbarSlot) {
                    slot = equippedSwordSlot != -1 ? equippedSwordSlot : inventorySwordSlot;
                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(slot), preferredSwordHotbarSlot, 2);
                    if (((Boolean)this.silent.getValue()).booleanValue()) {
                        PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                        this.serverOpen = false;
                    }
                    return;
                }
            }
            if (!(preferredPickaxeHotbarSlot < 0 || preferredPickaxeHotbarSlot > 8 || usedHotbarSlots.contains(preferredPickaxeHotbarSlot) || equippedPickaxeSlot == -1 && inventoryPickaxeSlot == -1)) {
                usedHotbarSlots.add(preferredPickaxeHotbarSlot);
                if (equippedPickaxeSlot != preferredPickaxeHotbarSlot && inventoryPickaxeSlot != preferredPickaxeHotbarSlot) {
                    slot = equippedPickaxeSlot != -1 ? equippedPickaxeSlot : inventoryPickaxeSlot;
                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(slot), preferredPickaxeHotbarSlot, 2);
                    if (((Boolean)this.silent.getValue()).booleanValue()) {
                        PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                        this.serverOpen = false;
                    }
                    return;
                }
            }
            if (!(preferredShovelHotbarSlot < 0 || preferredShovelHotbarSlot > 8 || usedHotbarSlots.contains(preferredShovelHotbarSlot) || equippedShovelSlot == -1 && inventoryShovelSlot == -1)) {
                usedHotbarSlots.add(preferredShovelHotbarSlot);
                if (equippedShovelSlot != preferredShovelHotbarSlot && inventoryShovelSlot != preferredShovelHotbarSlot) {
                    slot = equippedShovelSlot != -1 ? equippedShovelSlot : inventoryShovelSlot;
                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(slot), preferredShovelHotbarSlot, 2);
                    if (((Boolean)this.silent.getValue()).booleanValue()) {
                        PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                        this.serverOpen = false;
                    }
                    return;
                }
            }
            if (!(preferredAxeHotbarSlot < 0 || preferredAxeHotbarSlot > 8 || usedHotbarSlots.contains(preferredAxeHotbarSlot) || equippedAxeSlot == -1 && inventoryAxeSlot == -1)) {
                usedHotbarSlots.add(preferredAxeHotbarSlot);
                if (equippedAxeSlot != preferredAxeHotbarSlot && inventoryAxeSlot != preferredAxeHotbarSlot) {
                    slot = equippedAxeSlot != -1 ? equippedAxeSlot : inventoryAxeSlot;
                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(slot), preferredAxeHotbarSlot, 2);
                    if (((Boolean)this.silent.getValue()).booleanValue()) {
                        PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                        this.serverOpen = false;
                    }
                    return;
                }
            }
            if (preferredBlocksHotbarSlot >= 0 && preferredBlocksHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredBlocksHotbarSlot) && inventoryBlocksSlot != -1) {
                usedHotbarSlots.add(preferredBlocksHotbarSlot);
                if (inventoryBlocksSlot != preferredBlocksHotbarSlot) {
                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryBlocksSlot), preferredBlocksHotbarSlot, 2);
                    if (((Boolean)this.silent.getValue()).booleanValue()) {
                        PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                        this.serverOpen = false;
                    }
                    return;
                }
            }
            if (((Boolean)this.dropTrash.getValue()).booleanValue()) {
                int currentBlockCount = this.getStackSize(inventoryBlocksSlot);
                for (int i = 0; i < 36; ++i) {
                    ItemStack stack;
                    if (equippedArmorSlots.contains(i) || inventoryArmorSlots.contains(i) || equippedSwordSlot == i || inventorySwordSlot == i || equippedPickaxeSlot == i || inventoryPickaxeSlot == i || equippedShovelSlot == i || inventoryShovelSlot == i || equippedAxeSlot == i || inventoryAxeSlot == i || inventoryBlocksSlot == i || (stack = InvManager.mc.field_71439_g.field_71071_by.func_70301_a(i)) == null) continue;
                    boolean isBlock = ItemUtil.isBlock(stack);
                    if (!this.isProtectedItem(stack) && (ItemUtil.isNotSpecialItem(stack) || isBlock && currentBlockCount >= (Integer)this.blocks.getValue())) {
                        this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(i), 1, 4);
                        if (((Boolean)this.silent.getValue()).booleanValue()) {
                            PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                            this.serverOpen = false;
                        }
                        return;
                    }
                    if (!isBlock) continue;
                    currentBlockCount += stack.field_77994_a;
                }
            }
            if (((Boolean)this.silent.getValue()).booleanValue()) {
                PacketUtil.sendPacket(new C0DPacketCloseWindow(0));
                this.serverOpen = false;
            }
        }
    }

    @EventTarget
    public void onClick(WindowClickEvent event) {
        this.actionDelay = RandomUtils.nextInt((Integer)this.minDelay.getValue() + 1, (Integer)this.maxDelay.getValue() + 2);
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
}
