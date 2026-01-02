package myau.module.modules;

import java.util.Objects;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LeftClickMouseEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.util.ItemUtil;
import myau.util.KeyBindUtil;
import myau.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldSettings;

public class AutoClicker
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private boolean clickPending = false;
    private long clickDelay = 0L;
    private boolean blockHitPending = false;
    private long blockHitDelay = 0L;
    public final IntProperty minCPS = new IntProperty("min-cps", 8, 1, 20);
    public final IntProperty maxCPS = new IntProperty("max-cps", 12, 1, 20);
    public final BooleanProperty blockHit = new BooleanProperty("block-hit", false);
    public final FloatProperty blockHitTicks = new FloatProperty("block-hit-ticks", Float.valueOf(1.5f), Float.valueOf(1.0f), Float.valueOf(20.0f), this.blockHit::getValue);
    public final BooleanProperty weaponsOnly = new BooleanProperty("weapons-only", true);
    public final BooleanProperty allowTools = new BooleanProperty("allow-tools", false, this.weaponsOnly::getValue);
    public final BooleanProperty breakBlocks = new BooleanProperty("break-blocks", true);

    private long getNextClickDelay() {
        return 1000L / RandomUtil.nextLong(((Integer)this.minCPS.getValue()).intValue(), ((Integer)this.maxCPS.getValue()).intValue());
    }

    private long getBlockHitDelay() {
        return (long)(50.0f * ((Float)this.blockHitTicks.getValue()).floatValue());
    }

    private boolean isBreakingBlock() {
        return AutoClicker.mc.field_71476_x != null && AutoClicker.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private boolean canClick() {
        if (!((Boolean)this.weaponsOnly.getValue()).booleanValue() || ItemUtil.hasRawUnbreakingEnchant() || ((Boolean)this.allowTools.getValue()).booleanValue() && ItemUtil.isHoldingTool()) {
            if (((Boolean)this.breakBlocks.getValue()).booleanValue() && this.isBreakingBlock()) {
                WorldSettings.GameType gameType12 = AutoClicker.mc.field_71442_b.func_178889_l();
                return gameType12 != WorldSettings.GameType.SURVIVAL && gameType12 != WorldSettings.GameType.CREATIVE;
            }
            return true;
        }
        return false;
    }

    public AutoClicker() {
        super("AutoClicker", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.clickDelay > 0L) {
                this.clickDelay -= 50L;
            }
            if (this.blockHitDelay > 0L) {
                this.blockHitDelay -= 50L;
            }
            if (AutoClicker.mc.field_71462_r != null) {
                this.clickPending = false;
                this.blockHitPending = false;
            } else {
                if (this.clickPending) {
                    this.clickPending = false;
                    KeyBindUtil.updateKeyState(AutoClicker.mc.field_71474_y.field_74312_F.func_151463_i());
                }
                if (this.blockHitPending) {
                    this.blockHitPending = false;
                    KeyBindUtil.updateKeyState(AutoClicker.mc.field_71474_y.field_74313_G.func_151463_i());
                }
                if (this.isEnabled() && this.canClick() && AutoClicker.mc.field_71474_y.field_74312_F.func_151470_d()) {
                    if (!AutoClicker.mc.field_71439_g.func_71039_bw()) {
                        while (this.clickDelay <= 0L) {
                            this.clickPending = true;
                            this.clickDelay += this.getNextClickDelay();
                            KeyBindUtil.setKeyBindState(AutoClicker.mc.field_71474_y.field_74312_F.func_151463_i(), false);
                            KeyBindUtil.pressKeyOnce(AutoClicker.mc.field_71474_y.field_74312_F.func_151463_i());
                        }
                    }
                    if (((Boolean)this.blockHit.getValue()).booleanValue() && this.blockHitDelay <= 0L && AutoClicker.mc.field_71474_y.field_74313_G.func_151470_d() && ItemUtil.isHoldingSword()) {
                        this.blockHitPending = true;
                        KeyBindUtil.setKeyBindState(AutoClicker.mc.field_71474_y.field_74313_G.func_151463_i(), false);
                        if (!AutoClicker.mc.field_71439_g.func_71039_bw()) {
                            this.blockHitDelay += this.getBlockHitDelay();
                            KeyBindUtil.pressKeyOnce(AutoClicker.mc.field_71474_y.field_74313_G.func_151463_i());
                        }
                    }
                }
            }
        }
    }

    @EventTarget(value=4)
    public void onCLick(LeftClickMouseEvent event) {
        if (this.isEnabled() && !event.isCancelled() && !this.clickPending) {
            this.clickDelay += this.getNextClickDelay();
        }
    }

    @Override
    public void onEnabled() {
        this.clickDelay = 0L;
        this.blockHitDelay = 0L;
    }

    @Override
    public void verifyValue(String mode) {
        if (this.minCPS.getName().equals(mode)) {
            if ((Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
                this.maxCPS.setValue(this.minCPS.getValue());
            }
        } else if (this.maxCPS.getName().equals(mode) && (Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
            this.minCPS.setValue(this.maxCPS.getValue());
        }
    }

    @Override
    public String[] getSuffix() {
        String[] stringArray;
        if (Objects.equals(this.minCPS.getValue(), this.maxCPS.getValue())) {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = ((Integer)this.minCPS.getValue()).toString();
        } else {
            String[] stringArray3 = new String[1];
            stringArray = stringArray3;
            stringArray3[0] = String.format("%d-%d", this.minCPS.getValue(), this.maxCPS.getValue());
        }
        return stringArray;
    }
}
