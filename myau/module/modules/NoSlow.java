package myau.module.modules;

import myau.Myau;
import myau.enums.FloatModules;
import myau.event.EventTarget;
import myau.events.LivingUpdateEvent;
import myau.events.PlayerUpdateEvent;
import myau.events.RightClickMouseEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.BlockUtil;
import myau.util.ItemUtil;
import myau.util.PlayerUtil;
import myau.util.TeamUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class NoSlow
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int lastSlot = -1;
    private boolean isBlinking = false;
    private int blinkTimer = 0;
    private boolean wasUsingItem = false;
    private int hypixel2BlinkTimer = 0;
    private boolean isHypixel2Blinking = false;
    private int onGroundTicks = 0;
    private boolean isOpalBlocking = false;
    private int opalTimer = 0;
    private int disableBlinkTimer = 0;
    private boolean isDisableBlinking = false;
    private ModeProperty swordMode;
    private BooleanProperty opalStrictGroundCheck;
    private BooleanProperty opalCheckCollision;
    private BooleanProperty opalIgnoreWhenFalling;
    private BooleanProperty opalAllowSprint;
    private PercentProperty swordMotion;
    private BooleanProperty swordSprint;
    private IntProperty swordBlinkDelay;
    private IntProperty swordBlinkDuration;
    private ModeProperty foodMode;
    private PercentProperty foodMotion;
    private BooleanProperty foodSprint;
    private IntProperty foodBlinkDelay;
    private IntProperty foodBlinkDuration;
    private ModeProperty bowMode;
    private PercentProperty bowMotion;
    private BooleanProperty bowSprint;
    private IntProperty bowBlinkDelay;
    private IntProperty bowBlinkDuration;

    public NoSlow() {
        super("NoSlow", false);
        this.initProperties();
    }

    private void initProperties() {
        this.swordMode = new ModeProperty("sword-mode", 1, new String[]{"NONE", "VANILLA", "BLINK", "bzym", "Hypixel2", "BLINK2", "Rise", "Opal", "Disable"});
        this.opalStrictGroundCheck = new BooleanProperty("strict-ground", true, () -> (Integer)this.swordMode.getValue() == 7);
        this.opalCheckCollision = new BooleanProperty("check-collision", true, () -> (Integer)this.swordMode.getValue() == 7);
        this.opalIgnoreWhenFalling = new BooleanProperty("ignore-falling", false, () -> (Integer)this.swordMode.getValue() == 7);
        this.opalAllowSprint = new BooleanProperty("allow-sprint", true, () -> (Integer)this.swordMode.getValue() == 7);
        this.swordMotion = new PercentProperty("sword-motion", 100, () -> (Integer)this.swordMode.getValue() != 0 && (Integer)this.swordMode.getValue() != 7 && (Integer)this.swordMode.getValue() != 8);
        this.swordSprint = new BooleanProperty("sword-sprint", true, () -> (Integer)this.swordMode.getValue() != 0 && (Integer)this.swordMode.getValue() != 7 && (Integer)this.swordMode.getValue() != 8);
        this.swordBlinkDelay = new IntProperty("sword-blink-delay", 2, 1, 10, () -> (Integer)this.swordMode.getValue() == 2 || (Integer)this.swordMode.getValue() == 3 || (Integer)this.swordMode.getValue() == 4 || (Integer)this.swordMode.getValue() == 5 || (Integer)this.swordMode.getValue() == 7 || (Integer)this.swordMode.getValue() == 8);
        this.swordBlinkDuration = new IntProperty("sword-blink-duration", 1, 1, 5, () -> (Integer)this.swordMode.getValue() == 2 || (Integer)this.swordMode.getValue() == 3 || (Integer)this.swordMode.getValue() == 4 || (Integer)this.swordMode.getValue() == 5 || (Integer)this.swordMode.getValue() == 7 || (Integer)this.swordMode.getValue() == 8);
        this.foodMode = new ModeProperty("food-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT", "BLINK", "Grim"});
        this.foodMotion = new PercentProperty("food-motion", 100, () -> (Integer)this.foodMode.getValue() != 0 && (Integer)this.foodMode.getValue() != 4);
        this.foodSprint = new BooleanProperty("food-sprint", true, () -> (Integer)this.foodMode.getValue() != 0 && (Integer)this.foodMode.getValue() != 4);
        this.foodBlinkDelay = new IntProperty("food-blink-delay", 2, 1, 10, () -> (Integer)this.foodMode.getValue() == 3);
        this.foodBlinkDuration = new IntProperty("food-blink-duration", 1, 1, 5, () -> (Integer)this.foodMode.getValue() == 3);
        this.bowMode = new ModeProperty("bow-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT", "BLINK"});
        this.bowMotion = new PercentProperty("bow-motion", 100, () -> (Integer)this.bowMode.getValue() != 0);
        this.bowSprint = new BooleanProperty("bow-sprint", true, () -> (Integer)this.bowMode.getValue() != 0);
        this.bowBlinkDelay = new IntProperty("bow-blink-delay", 2, 1, 10, () -> (Integer)this.bowMode.getValue() == 3);
        this.bowBlinkDuration = new IntProperty("bow-blink-duration", 1, 1, 5, () -> (Integer)this.bowMode.getValue() == 3);
    }

    public boolean isOpalMode() {
        return (Integer)this.swordMode.getValue() == 7 && ItemUtil.isHoldingSword();
    }

    public boolean isDisableMode() {
        return (Integer)this.swordMode.getValue() == 8 && ItemUtil.isHoldingSword();
    }

    public boolean isSwordActive() {
        return (Integer)this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword();
    }

    public boolean isFoodActive() {
        return (Integer)this.foodMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return (Integer)this.bowMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isGrimFoodMode() {
        return (Integer)this.foodMode.getValue() == 4 && ItemUtil.isEating();
    }

    public boolean isFloatMode() {
        return (Integer)this.foodMode.getValue() == 2 && ItemUtil.isEating() || (Integer)this.bowMode.getValue() == 2 && ItemUtil.isUsingBow();
    }

    public boolean isBlinkMode() {
        return (Integer)this.swordMode.getValue() == 2 && ItemUtil.isHoldingSword() || (Integer)this.foodMode.getValue() == 3 && ItemUtil.isEating() || (Integer)this.bowMode.getValue() == 3 && ItemUtil.isUsingBow();
    }

    public boolean isBzymMode() {
        return (Integer)this.swordMode.getValue() == 3 && ItemUtil.isHoldingSword();
    }

    public boolean isHypixel2Mode() {
        return (Integer)this.swordMode.getValue() == 4 && ItemUtil.isHoldingSword();
    }

    public boolean isBlink2Mode() {
        return (Integer)this.swordMode.getValue() == 5 && ItemUtil.isHoldingSword();
    }

    public boolean isRiseMode() {
        return (Integer)this.swordMode.getValue() == 6 && ItemUtil.isHoldingSword();
    }

    public boolean isAnyActive() {
        return NoSlow.mc.field_71439_g != null && NoSlow.mc.field_71439_g.func_71039_bw() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
    }

    public boolean canSprint() {
        return this.isSwordActive() && (Boolean)this.swordSprint.getValue() != false || this.isFoodActive() && (Boolean)this.foodSprint.getValue() != false || this.isBowActive() && (Boolean)this.bowSprint.getValue() != false;
    }

    public int getMotionMultiplier() {
        if (ItemUtil.isHoldingSword()) {
            return (Integer)this.swordMotion.getValue();
        }
        if (ItemUtil.isEating()) {
            return (Integer)this.foodMotion.getValue();
        }
        return ItemUtil.isUsingBow() ? (Integer)this.bowMotion.getValue() : 100;
    }

    private boolean shouldBlink() {
        if (!(this.isBlinkMode() || this.isBlink2Mode() || this.isDisableMode())) {
            return false;
        }
        ++this.blinkTimer;
        int delay = 2;
        int duration = 1;
        if (ItemUtil.isHoldingSword()) {
            delay = (Integer)this.swordBlinkDelay.getValue();
            duration = (Integer)this.swordBlinkDuration.getValue();
        } else if (ItemUtil.isEating()) {
            delay = (Integer)this.foodBlinkDelay.getValue();
            duration = (Integer)this.foodBlinkDuration.getValue();
        } else if (ItemUtil.isUsingBow()) {
            delay = (Integer)this.bowBlinkDelay.getValue();
            duration = (Integer)this.bowBlinkDuration.getValue();
        }
        int totalCycle = delay + duration;
        int currentPhase = this.blinkTimer % totalCycle;
        if (currentPhase < delay) {
            this.isBlinking = false;
            return false;
        }
        this.isBlinking = true;
        return true;
    }

    private void sendBzymC09() {
        if (NoSlow.mc.field_71439_g == null || mc.func_147114_u() == null) {
            return;
        }
        int current = NoSlow.mc.field_71439_g.field_71071_by.field_70461_c;
        int next = (current + 1) % 9;
        mc.func_147114_u().func_147297_a((Packet)new C09PacketHeldItemChange(next));
        mc.func_147114_u().func_147297_a((Packet)new C09PacketHeldItemChange(current));
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (!this.isEnabled() || NoSlow.mc.field_71439_g == null) {
            this.resetAllStates();
            return;
        }
        this.onGroundTicks = NoSlow.mc.field_71439_g.field_70122_E ? ++this.onGroundTicks : 0;
        boolean isGrimActive = this.isGrimFoodMode();
        boolean isOpalActive = this.isOpalMode();
        boolean isDisableActive = this.isDisableMode();
        if (isOpalActive) {
            this.handleOpalMode();
            return;
        }
        if (isDisableActive) {
            this.handleDisableMode();
            return;
        }
        if (isGrimActive) {
            this.handleGrimMode();
            return;
        }
        if (this.isAnyActive()) {
            boolean isMovingForward;
            float multiplier = (float)this.getMotionMultiplier() / 100.0f;
            NoSlow.mc.field_71439_g.field_71158_b.field_78900_b *= multiplier;
            NoSlow.mc.field_71439_g.field_71158_b.field_78902_a *= multiplier;
            boolean shouldSprint = false;
            boolean playerWantsToSprint = NoSlow.mc.field_71474_y.field_151444_V.func_151470_d();
            boolean bl = isMovingForward = NoSlow.mc.field_71439_g.field_71158_b.field_78900_b > 0.1f;
            if (this.canSprint() && playerWantsToSprint && isMovingForward) {
                shouldSprint = true;
            }
            NoSlow.mc.field_71439_g.func_70031_b(shouldSprint);
        }
        if (this.isBzymMode()) {
            if (PlayerUtil.isUsingItem()) {
                this.sendBzymC09();
            }
            return;
        }
        if (this.isHypixel2Mode()) {
            this.handleHypixel2Mode();
            return;
        }
        if (this.isBlink2Mode()) {
            this.handleBlink2Mode();
            return;
        }
        if (this.isBlinkMode() && this.shouldBlink() && this.isSwordActive()) {
            NoSlow.mc.field_71439_g.func_71034_by();
        }
    }

    private void handleDisableMode() {
        boolean isMovingForward;
        int duration;
        boolean isCurrentlyBlocking = PlayerUtil.isUsingItem();
        ++this.disableBlinkTimer;
        int delay = (Integer)this.swordBlinkDelay.getValue();
        int totalCycle = delay + (duration = ((Integer)this.swordBlinkDuration.getValue()).intValue());
        int currentPhase = this.disableBlinkTimer % totalCycle;
        if (currentPhase < delay) {
            this.isDisableBlinking = false;
        } else {
            this.isDisableBlinking = true;
            this.sendBzymC09();
            if (isCurrentlyBlocking) {
                NoSlow.mc.field_71439_g.func_71034_by();
            }
        }
        boolean playerWantsToSprint = NoSlow.mc.field_71474_y.field_151444_V.func_151470_d();
        boolean bl = isMovingForward = NoSlow.mc.field_71439_g.field_71158_b.field_78900_b > 0.1f;
        if (playerWantsToSprint && isMovingForward) {
            NoSlow.mc.field_71439_g.func_70031_b(true);
        }
        this.wasUsingItem = isCurrentlyBlocking;
    }

    private void handleOpalMode() {
        boolean shouldUnblock;
        if (((Boolean)this.opalIgnoreWhenFalling.getValue()).booleanValue() && !NoSlow.mc.field_71439_g.field_70122_E) {
            this.isOpalBlocking = false;
            return;
        }
        if (((Boolean)this.opalCheckCollision.getValue()).booleanValue() && !NoSlow.mc.field_71439_g.field_70124_G) {
            this.isOpalBlocking = false;
            return;
        }
        if (((Boolean)this.opalStrictGroundCheck.getValue()).booleanValue() && !this.isPlayerFullyOnGround()) {
            this.isOpalBlocking = false;
            return;
        }
        boolean bl = shouldUnblock = (NoSlow.mc.field_71439_g.func_70632_aY() || this.isOpalBlocking) && this.isMoving();
        if (shouldUnblock) {
            int duration;
            ++this.opalTimer;
            int delay = (Integer)this.swordBlinkDelay.getValue();
            int totalCycle = delay + (duration = ((Integer)this.swordBlinkDuration.getValue()).intValue());
            int currentPhase = this.opalTimer % totalCycle;
            if (currentPhase < delay) {
                if (this.isOpalBlocking) {
                    this.isOpalBlocking = false;
                    NoSlow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.field_177992_a, EnumFacing.DOWN));
                }
            } else if (!this.isOpalBlocking) {
                this.isOpalBlocking = true;
                NoSlow.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C08PacketPlayerBlockPlacement(NoSlow.mc.field_71439_g.field_71071_by.func_70448_g()));
            }
        } else {
            this.isOpalBlocking = false;
            this.opalTimer = 0;
        }
        if (((Boolean)this.opalAllowSprint.getValue()).booleanValue() && this.isMoving()) {
            boolean playerWantsToSprint = NoSlow.mc.field_71474_y.field_151444_V.func_151470_d();
            boolean isMovingForward = NoSlow.mc.field_71439_g.field_71158_b.field_78900_b > 0.1f;
            NoSlow.mc.field_71439_g.func_70031_b(playerWantsToSprint && isMovingForward);
        }
    }

    private boolean isMoving() {
        return NoSlow.mc.field_71439_g != null && (NoSlow.mc.field_71439_g.field_70701_bs != 0.0f || NoSlow.mc.field_71439_g.field_70702_br != 0.0f);
    }

    private boolean isPlayerFullyOnGround() {
        double[][] offsets;
        boolean touchingGround = false;
        for (double[] offset : offsets = new double[][]{{0.0, 0.0}, {-0.35, -0.35}, {-0.35, 0.35}, {0.35, 0.35}, {0.35, -0.35}}) {
            double offsetX = offset[0];
            double offsetZ = offset[1];
            double posX = offsetX + NoSlow.mc.field_71439_g.field_70165_t;
            double posY = -0.5 + NoSlow.mc.field_71439_g.field_70163_u;
            double posZ = offsetZ + NoSlow.mc.field_71439_g.field_70161_v;
            double lastPosX = offsetX + NoSlow.mc.field_71439_g.field_70142_S;
            double lastPosY = -0.5 + NoSlow.mc.field_71439_g.field_70137_T;
            double lastPosZ = offsetZ + NoSlow.mc.field_71439_g.field_70136_U;
            if (!this.isPositionOnGround(posX, posY, posZ) || !this.isPositionOnGround(lastPosX, lastPosY, lastPosZ)) continue;
            touchingGround = true;
            break;
        }
        return touchingGround;
    }

    private boolean isPositionOnGround(double posX, double posY, double posZ) {
        boolean isOnSlab = (double)Math.round((posY - (double)((int)posY)) * 10.0) / 10.0 == 0.5;
        double checkY = posY - (isOnSlab ? 0.0 : 0.1);
        BlockPos checkPos = new BlockPos(posX, checkY, posZ);
        Block blockUnder = NoSlow.mc.field_71441_e.func_180495_p(checkPos).func_177230_c();
        boolean feetBlockAir = isOnSlab ? blockUnder.func_149688_o() == Material.field_151579_a : blockUnder instanceof BlockSlab && !blockUnder.func_149730_j() || blockUnder.func_149688_o() == Material.field_151579_a;
        boolean headClear = !NoSlow.mc.field_71441_e.func_180495_p(new BlockPos(posX, posY + 1.5, posZ)).func_177230_c().func_149637_q();
        return !feetBlockAir && headClear;
    }

    private void handleGrimMode() {
        if (this.onGroundTicks % 3 != 0 && NoSlow.mc.field_71439_g.func_71052_bv() >= 4) {
            boolean playerWantsToSprint = NoSlow.mc.field_71474_y.field_151444_V.func_151470_d();
            boolean isMovingForward = NoSlow.mc.field_71439_g.field_71158_b.field_78900_b > 0.1f;
            NoSlow.mc.field_71439_g.func_70031_b(playerWantsToSprint && isMovingForward);
        } else {
            boolean playerWantsToSprint = NoSlow.mc.field_71474_y.field_151444_V.func_151470_d();
            boolean isMovingForward = NoSlow.mc.field_71439_g.field_71158_b.field_78900_b > 0.1f;
            NoSlow.mc.field_71439_g.func_70031_b(playerWantsToSprint && isMovingForward);
        }
    }

    private void handleHypixel2Mode() {
        boolean isUsingNow = PlayerUtil.isUsingItem();
        if (isUsingNow && !this.wasUsingItem) {
            this.sendBzymC09();
            this.hypixel2BlinkTimer = 0;
            this.isHypixel2Blinking = true;
        }
        this.wasUsingItem = isUsingNow;
        if (this.isHypixel2Blinking) {
            int duration;
            ++this.hypixel2BlinkTimer;
            int delay = (Integer)this.swordBlinkDelay.getValue();
            int total = delay + (duration = ((Integer)this.swordBlinkDuration.getValue()).intValue());
            int phase = this.hypixel2BlinkTimer % total;
            if (phase >= delay) {
                NoSlow.mc.field_71439_g.func_71034_by();
                this.isHypixel2Blinking = false;
            }
        }
    }

    private void handleBlink2Mode() {
        boolean isCurrentlyBlocking;
        boolean bl = isCurrentlyBlocking = this.isSwordActive() && PlayerUtil.isUsingItem();
        if (this.shouldBlink()) {
            if (this.isSwordActive()) {
                NoSlow.mc.field_71439_g.func_71034_by();
            }
            isCurrentlyBlocking = false;
        }
        this.wasUsingItem = isCurrentlyBlocking;
    }

    private void resetAllStates() {
        this.isBlinking = false;
        this.wasUsingItem = false;
        this.isHypixel2Blinking = false;
        this.isOpalBlocking = false;
        this.isDisableBlinking = false;
        this.onGroundTicks = 0;
        this.blinkTimer = 0;
        this.hypixel2BlinkTimer = 0;
        this.opalTimer = 0;
        this.disableBlinkTimer = 0;
    }

    @EventTarget(value=3)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled() && this.isRiseMode()) {
            if (PlayerUtil.isUsingItem()) {
                this.sendBzymC09();
            }
            return;
        }
        if (this.isEnabled() && this.isFloatMode()) {
            int item = NoSlow.mc.field_71439_g.field_71071_by.field_70461_c;
            if (this.lastSlot != item && PlayerUtil.isUsingItem()) {
                this.lastSlot = item;
                Myau.floatManager.setFloatState(true, FloatModules.NO_SLOW);
            }
        } else {
            this.lastSlot = -1;
            Myau.floatManager.setFloatState(false, FloatModules.NO_SLOW);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (!this.isEnabled() || NoSlow.mc.field_71439_g == null) {
            return;
        }
        if (NoSlow.mc.field_71476_x != null) {
            switch (NoSlow.mc.field_71476_x.field_72313_a) {
                case BLOCK: {
                    BlockPos pos = NoSlow.mc.field_71476_x.func_178782_a();
                    if (!BlockUtil.isInteractable(pos) || PlayerUtil.isSneaking()) break;
                    return;
                }
                case ENTITY: {
                    Entity hit = NoSlow.mc.field_71476_x.field_72308_g;
                    if (!(hit instanceof EntityVillager) && (!(hit instanceof EntityLivingBase) || !TeamUtil.isShop((EntityLivingBase)hit))) break;
                    return;
                }
            }
        }
        if (this.isFloatMode() && !Myau.floatManager.isPredicted() && NoSlow.mc.field_71439_g.field_70122_E) {
            event.setCancelled(true);
            NoSlow.mc.field_71439_g.field_70181_x = 0.42f;
        }
    }

    @Override
    public void onEnabled() {
        this.resetAllStates();
    }

    @Override
    public void onDisabled() {
        this.resetAllStates();
        if (NoSlow.mc.field_71439_g != null) {
            NoSlow.mc.field_71439_g.func_71034_by();
        }
    }
}
