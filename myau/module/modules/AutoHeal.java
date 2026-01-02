package myau.module.modules;

import myau.event.EventTarget;
import myau.events.HitBlockEvent;
import myau.events.LeftClickMouseEvent;
import myau.events.RightClickMouseEvent;
import myau.events.SwapItemEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import myau.util.PacketUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;

public class AutoHeal
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil timer = new TimerUtil();
    private boolean shouldHeal = false;
    private int prevSlot = -1;
    public final PercentProperty health = new PercentProperty("health", 35);
    public final IntProperty delay = new IntProperty("delay", 4000, 0, 5000);
    public final BooleanProperty regenCheck = new BooleanProperty("regen-check", false);

    private int findHealingItem() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoHeal.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack == null || !stack.func_82837_s()) continue;
            String name = stack.func_82833_r();
            if (stack.func_77973_b() instanceof ItemSkull && (name.contains("\u00a76") && name.contains("Golden Head") || name.matches("\\S+\u00a7c's Head"))) {
                return i;
            }
            if (!(stack.func_77973_b() instanceof ItemSoup) || (!name.contains("\u00a7a") || !name.contains("Tasty Soup")) && (!name.contains("\u00a7a") || !name.contains("Assist Soup"))) continue;
            return i;
        }
        return -1;
    }

    private boolean hasRegenEffect() {
        return (Boolean)this.regenCheck.getValue() != false && AutoHeal.mc.field_71439_g.func_70644_a(Potion.field_76428_l);
    }

    public AutoHeal() {
        super("AutoHeal", false);
    }

    public boolean isSwitching() {
        return this.prevSlot != -1;
    }

    @EventTarget(value=1)
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) {
            this.prevSlot = -1;
        } else {
            switch (event.getType()) {
                case PRE: {
                    int slot;
                    boolean precent;
                    boolean bl = precent = (float)Math.ceil(AutoHeal.mc.field_71439_g.func_110143_aJ() + AutoHeal.mc.field_71439_g.func_110139_bj()) / AutoHeal.mc.field_71439_g.func_110138_aP() <= (float)((Integer)this.health.getValue()).intValue() / 100.0f;
                    if (this.shouldHeal && precent && !this.hasRegenEffect() && this.timer.hasTimeElapsed(((Integer)this.delay.getValue()).intValue()) && (slot = this.findHealingItem()) != -1) {
                        this.prevSlot = AutoHeal.mc.field_71439_g.field_71071_by.field_70461_c;
                        AutoHeal.mc.field_71439_g.field_71071_by.field_70461_c = slot;
                        ((IAccessorPlayerControllerMP)AutoHeal.mc.field_71442_b).callSyncCurrentPlayItem();
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(AutoHeal.mc.field_71439_g.func_70694_bm()));
                        this.timer.reset();
                    }
                    this.shouldHeal = precent;
                    break;
                }
                case POST: {
                    if (this.prevSlot == -1) break;
                    AutoHeal.mc.field_71439_g.field_71071_by.field_70461_c = this.prevSlot;
                    this.prevSlot = -1;
                }
            }
        }
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isEnabled() && this.isSwitching()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled() && this.isSwitching()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isEnabled() && this.isSwitching()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onSwap(SwapItemEvent event) {
        if (this.isEnabled() && this.isSwitching()) {
            event.setCancelled(true);
        }
    }
}
