package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventTarget;
import myau.events.KeyEvent;
import myau.events.PlayerUpdateEvent;
import myau.module.Module;
import myau.module.modules.LongJump;
import myau.property.properties.FloatProperty;
import myau.property.properties.ModeProperty;
import myau.util.PlayerUtil;
import myau.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

public class AntiVoid
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private boolean isInVoid = false;
    private boolean wasInVoid = false;
    private double[] lastSafePosition = null;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"BLINK"});
    public final FloatProperty distance = new FloatProperty("distance", Float.valueOf(5.0f), Float.valueOf(0.0f), Float.valueOf(16.0f));

    private void resetBlink() {
        Myau.blinkManager.setBlinkState(false, BlinkModules.ANTI_VOID);
        this.lastSafePosition = null;
    }

    private boolean canUseAntiVoid() {
        LongJump longJump = (LongJump)Myau.moduleManager.modules.get(LongJump.class);
        return !longJump.isJumping();
    }

    public AntiVoid() {
        super("AntiVoid", false);
    }

    @EventTarget(value=4)
    public void onUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled()) {
            boolean bl = this.isInVoid = !AntiVoid.mc.field_71439_g.field_71075_bZ.field_75101_c && PlayerUtil.isInWater();
            if ((Integer)this.mode.getValue() == 0) {
                float height;
                float subWidth;
                if (!this.isInVoid) {
                    this.resetBlink();
                }
                if (this.lastSafePosition != null && PlayerUtil.checkInWater(new AxisAlignedBB(this.lastSafePosition[0] - (double)(subWidth = AntiVoid.mc.field_71439_g.field_70130_N / 2.0f), this.lastSafePosition[1], this.lastSafePosition[2] - (double)subWidth, this.lastSafePosition[0] + (double)subWidth, this.lastSafePosition[1] + (double)(height = AntiVoid.mc.field_71439_g.field_70131_O), this.lastSafePosition[2] + (double)subWidth))) {
                    this.resetBlink();
                }
                if (!this.wasInVoid && this.isInVoid && this.canUseAntiVoid()) {
                    Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                    if (Myau.blinkManager.setBlinkState(true, BlinkModules.ANTI_VOID)) {
                        this.lastSafePosition = new double[]{AntiVoid.mc.field_71439_g.field_70169_q, AntiVoid.mc.field_71439_g.field_70167_r, AntiVoid.mc.field_71439_g.field_70166_s};
                    }
                }
                if (Myau.blinkManager.getBlinkingModule() == BlinkModules.ANTI_VOID && this.lastSafePosition != null && this.lastSafePosition[1] - (double)((Float)this.distance.getValue()).floatValue() > AntiVoid.mc.field_71439_g.field_70163_u) {
                    Myau.blinkManager.blinkedPackets.offerFirst((Packet<?>)new C03PacketPlayer.C04PacketPlayerPosition(this.lastSafePosition[0], this.lastSafePosition[1] - RandomUtil.nextDouble(10.0, 20.0), this.lastSafePosition[2], false));
                    this.resetBlink();
                }
            }
            this.wasInVoid = this.isInVoid;
        }
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        ItemStack currentItem;
        if (event.getKey() == AntiVoid.mc.field_71474_y.field_74313_G.func_151463_i() && (currentItem = AntiVoid.mc.field_71439_g.field_71071_by.func_70448_g()) != null && currentItem.func_77973_b() instanceof ItemEnderPearl) {
            this.resetBlink();
        }
    }

    @Override
    public void onEnabled() {
        this.isInVoid = false;
        this.wasInVoid = false;
        this.resetBlink();
    }

    @Override
    public void onDisabled() {
        Myau.blinkManager.setBlinkState(false, BlinkModules.ANTI_VOID);
    }

    @Override
    public void verifyValue(String mode) {
        if (this.isEnabled()) {
            this.onDisabled();
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}
