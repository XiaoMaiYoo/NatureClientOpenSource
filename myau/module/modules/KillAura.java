package myau.module.modules;

import com.google.common.base.CaseFormat;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.event.EventManager;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.AttackEvent;
import myau.events.CancelUseEvent;
import myau.events.HitBlockEvent;
import myau.events.LeftClickMouseEvent;
import myau.events.MoveInputEvent;
import myau.events.PacketEvent;
import myau.events.Render3DEvent;
import myau.events.RightClickMouseEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.module.modules.AutoHeal;
import myau.module.modules.BedNuker;
import myau.module.modules.HUD;
import myau.module.modules.NoSlow;
import myau.module.modules.Scaffold;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ChatUtil;
import myau.util.ItemUtil;
import myau.util.KeyBindUtil;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.PlayerUtil;
import myau.util.RandomUtil;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings;

public class KillAura
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat df = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));
    private final TimerUtil timer = new TimerUtil();
    private AttackData target = null;
    private int switchTick = 0;
    private boolean hitRegistered = false;
    private boolean blockingState = false;
    private boolean isBlocking = false;
    private boolean fakeBlockState = false;
    private boolean blinkReset = false;
    private boolean swapped = false;
    private long attackDelayMS = 0L;
    private int blockTick = 0;
    private int lastTickProcessed = 0;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"SINGLE", "SWITCH"});
    public final ModeProperty sort = new ModeProperty("sort", 0, new String[]{"DISTANCE", "HEALTH", "HURT_TIME", "FOV"});
    public final ModeProperty autoBlock = new ModeProperty("auto-block", 3, new String[]{"NONE", "VANILLA", "SPOOF", "Grim", "BLINK", "INTERACT", "SWAP", "LEGIT", "FAKE", "NEW", "blinkLESS", "Rise", "Opal"});
    public final BooleanProperty autoBlockRequirePress = new BooleanProperty("auto-block-require-press", false);
    public final FloatProperty autoBlockCPS = new FloatProperty("auto-block-aps", Float.valueOf(10.0f), Float.valueOf(1.0f), Float.valueOf(20.0f));
    public final FloatProperty autoBlockRange = new FloatProperty("auto-block-range", Float.valueOf(6.0f), Float.valueOf(3.0f), Float.valueOf(8.0f));
    public final FloatProperty swingRange = new FloatProperty("swing-range", Float.valueOf(3.5f), Float.valueOf(3.0f), Float.valueOf(6.0f));
    public final FloatProperty attackRange = new FloatProperty("attack-range", Float.valueOf(3.0f), Float.valueOf(3.0f), Float.valueOf(6.0f));
    public final IntProperty fov = new IntProperty("fov", 360, 30, 360);
    public final IntProperty minCPS = new IntProperty("min-aps", 14, 1, 20);
    public final IntProperty maxCPS = new IntProperty("max-aps", 14, 1, 20);
    public final IntProperty switchDelay = new IntProperty("switch-delay", 150, 0, 1000);
    public final ModeProperty rotations = new ModeProperty("rotations", 2, new String[]{"NONE", "LEGIT", "SILENT", "LOCK_VIEW"});
    public final ModeProperty moveFix = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
    public final PercentProperty smoothing = new PercentProperty("smoothing", 0);
    public final IntProperty angleStep = new IntProperty("angle-step", 90, 30, 180);
    public final BooleanProperty throughWalls = new BooleanProperty("through-walls", true);
    public final BooleanProperty requirePress = new BooleanProperty("require-press", false);
    public final BooleanProperty allowMining = new BooleanProperty("allow-mining", true);
    public final BooleanProperty weaponsOnly = new BooleanProperty("weapons-only", true);
    public final BooleanProperty allowTools = new BooleanProperty("allow-tools", false, this.weaponsOnly::getValue);
    public final BooleanProperty inventoryCheck = new BooleanProperty("inventory-check", true);
    public final BooleanProperty botCheck = new BooleanProperty("bot-check", true);
    public final BooleanProperty players = new BooleanProperty("players", true);
    public final BooleanProperty bosses = new BooleanProperty("bosses", false);
    public final BooleanProperty mobs = new BooleanProperty("mobs", false);
    public final BooleanProperty animals = new BooleanProperty("animals", false);
    public final BooleanProperty golems = new BooleanProperty("golems", false);
    public final BooleanProperty silverfish = new BooleanProperty("silverfish", false);
    public final BooleanProperty teams = new BooleanProperty("teams", true);
    public final ModeProperty showTarget = new ModeProperty("show-target", 0, new String[]{"NONE", "DEFAULT", "HUD"});
    public final ModeProperty debugLog = new ModeProperty("debug-log", 0, new String[]{"NONE", "HEALTH"});

    private long getAttackDelay() {
        return this.isBlocking ? (long)(1000.0f / ((Float)this.autoBlockCPS.getValue()).floatValue()) : 1000L / RandomUtil.nextLong(((Integer)this.minCPS.getValue()).intValue(), ((Integer)this.maxCPS.getValue()).intValue());
    }

    private boolean performAttack(float yaw, float pitch) {
        if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
            if (this.isPlayerBlocking() && (Integer)this.autoBlock.getValue() != 1) {
                return false;
            }
            if (this.attackDelayMS > 0L) {
                return false;
            }
            this.attackDelayMS += this.getAttackDelay();
            KillAura.mc.field_71439_g.func_71038_i();
            if (!((Integer)this.rotations.getValue() == 0 && this.isBoxInAttackRange(this.target.getBox()) || RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, (double)((Float)this.attackRange.getValue()).floatValue()) != null)) {
                return false;
            }
            AttackEvent event = new AttackEvent((Entity)this.target.getEntity());
            EventManager.call(event);
            ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).callSyncCurrentPlayItem();
            PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target.getEntity(), C02PacketUseEntity.Action.ATTACK));
            if (KillAura.mc.field_71442_b.func_178889_l() != WorldSettings.GameType.SPECTATOR) {
                PlayerUtil.attackEntity((Entity)this.target.getEntity());
            }
            this.hitRegistered = true;
            return true;
        }
        return false;
    }

    private void sendUseItem() {
        ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).callSyncCurrentPlayItem();
        this.startBlock(KillAura.mc.field_71439_g.func_70694_bm());
    }

    private void startBlock(ItemStack itemStack) {
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(itemStack));
        KillAura.mc.field_71439_g.func_71008_a(itemStack, itemStack.func_77988_m());
        this.blockingState = true;
    }

    private void stopBlock() {
        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.field_177992_a, EnumFacing.DOWN));
        KillAura.mc.field_71439_g.func_71034_by();
        this.blockingState = false;
    }

    private void interactAttack(float yaw, float pitch) {
        MovingObjectPosition mop;
        if (this.target != null && (mop = RotationUtil.rayTrace(this.target.getBox(), yaw, pitch, 8.0)) != null) {
            ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).callSyncCurrentPlayItem();
            PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target.getEntity(), new Vec3(mop.field_72307_f.field_72450_a - this.target.getX(), mop.field_72307_f.field_72448_b - this.target.getY(), mop.field_72307_f.field_72449_c - this.target.getZ())));
            PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target.getEntity(), C02PacketUseEntity.Action.INTERACT));
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(KillAura.mc.field_71439_g.func_70694_bm()));
            KillAura.mc.field_71439_g.func_71008_a(KillAura.mc.field_71439_g.func_70694_bm(), KillAura.mc.field_71439_g.func_70694_bm().func_77988_m());
            this.blockingState = true;
        }
    }

    private boolean canAttack() {
        if (((Boolean)this.inventoryCheck.getValue()).booleanValue() && KillAura.mc.field_71462_r instanceof GuiContainer) {
            return false;
        }
        if (!((Boolean)this.weaponsOnly.getValue()).booleanValue() || ItemUtil.hasRawUnbreakingEnchant() || ((Boolean)this.allowTools.getValue()).booleanValue() && ItemUtil.isHoldingTool()) {
            if (((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).getIsHittingBlock()) {
                return false;
            }
            if ((ItemUtil.isEating() || ItemUtil.isUsingBow()) && PlayerUtil.isUsingItem()) {
                return false;
            }
            AutoHeal autoHeal = (AutoHeal)Myau.moduleManager.modules.get(AutoHeal.class);
            if (autoHeal.isEnabled() && autoHeal.isSwitching()) {
                return false;
            }
            BedNuker bedNuker = (BedNuker)Myau.moduleManager.modules.get(BedNuker.class);
            if (bedNuker.isEnabled() && bedNuker.isReady()) {
                return false;
            }
            if (Myau.moduleManager.modules.get(Scaffold.class).isEnabled()) {
                return false;
            }
            if (((Boolean)this.requirePress.getValue()).booleanValue()) {
                return PlayerUtil.isAttacking();
            }
            return (Boolean)this.allowMining.getValue() == false || !KillAura.mc.field_71476_x.field_72313_a.equals((Object)MovingObjectPosition.MovingObjectType.BLOCK) || !PlayerUtil.isAttacking();
        }
        return false;
    }

    private boolean canAutoBlock() {
        if (!ItemUtil.isHoldingSword()) {
            return false;
        }
        return (Boolean)this.autoBlockRequirePress.getValue() == false || PlayerUtil.isUsingItem();
    }

    public boolean hasValidTarget() {
        return KillAura.mc.field_71441_e.field_72996_f.stream().anyMatch(entity -> entity instanceof EntityLivingBase && this.isValidTarget((EntityLivingBase)entity) && this.isInBlockRange((EntityLivingBase)entity));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isValidTarget(EntityLivingBase entityLivingBase) {
        if (!KillAura.mc.field_71441_e.field_72996_f.contains(entityLivingBase)) {
            return false;
        }
        if (entityLivingBase == KillAura.mc.field_71439_g) return false;
        if (entityLivingBase == KillAura.mc.field_71439_g.field_70154_o) return false;
        if (entityLivingBase == mc.func_175606_aa()) return false;
        if (entityLivingBase == KillAura.mc.func_175606_aa().field_70154_o) {
            return false;
        }
        if (entityLivingBase.field_70725_aQ > 0) {
            return false;
        }
        if (RotationUtil.angleToEntity((Entity)entityLivingBase) > ((Integer)this.fov.getValue()).floatValue()) {
            return false;
        }
        if (!((Boolean)this.throughWalls.getValue()).booleanValue() && RotationUtil.rayTrace((Entity)entityLivingBase) != null) {
            return false;
        }
        if (entityLivingBase instanceof EntityOtherPlayerMP) {
            if (!((Boolean)this.players.getValue()).booleanValue()) {
                return false;
            }
            if (TeamUtil.isFriend((EntityPlayer)entityLivingBase)) {
                return false;
            }
            if (((Boolean)this.teams.getValue()).booleanValue()) {
                if (TeamUtil.isSameTeam((EntityPlayer)entityLivingBase)) return false;
            }
            if ((Boolean)this.botCheck.getValue() == false) return true;
            if (TeamUtil.isBot((EntityPlayer)entityLivingBase)) return false;
            return true;
        }
        if (entityLivingBase instanceof EntityDragon) return (Boolean)this.bosses.getValue();
        if (entityLivingBase instanceof EntityWither) {
            return (Boolean)this.bosses.getValue();
        }
        if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
            if (entityLivingBase instanceof EntityAnimal) return (Boolean)this.animals.getValue();
            if (entityLivingBase instanceof EntityBat) return (Boolean)this.animals.getValue();
            if (entityLivingBase instanceof EntitySquid) return (Boolean)this.animals.getValue();
            if (entityLivingBase instanceof EntityVillager) {
                return (Boolean)this.animals.getValue();
            }
            if (entityLivingBase instanceof EntityIronGolem) return false;
            if ((Boolean)this.golems.getValue() == false) return false;
            if ((Boolean)this.teams.getValue() == false) return true;
            if (TeamUtil.hasTeamColor(entityLivingBase)) return false;
            return true;
        }
        if (!(entityLivingBase instanceof EntitySilverfish)) {
            return (Boolean)this.mobs.getValue();
        }
        if ((Boolean)this.silverfish.getValue() == false) return false;
        if ((Boolean)this.teams.getValue() == false) return true;
        if (TeamUtil.hasTeamColor(entityLivingBase)) return false;
        return true;
    }

    private boolean isInRange(EntityLivingBase entityLivingBase) {
        return this.isInBlockRange(entityLivingBase) || this.isInSwingRange(entityLivingBase) || this.isInAttackRange(entityLivingBase);
    }

    private boolean isInBlockRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity((Entity)entityLivingBase) <= (double)((Float)this.autoBlockRange.getValue()).floatValue();
    }

    private boolean isInSwingRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity((Entity)entityLivingBase) <= (double)((Float)this.swingRange.getValue()).floatValue();
    }

    private boolean isBoxInSwingRange(AxisAlignedBB axisAlignedBB) {
        return RotationUtil.distanceToBox(axisAlignedBB) <= (double)((Float)this.swingRange.getValue()).floatValue();
    }

    private boolean isInAttackRange(EntityLivingBase entityLivingBase) {
        return RotationUtil.distanceToEntity((Entity)entityLivingBase) <= (double)((Float)this.attackRange.getValue()).floatValue();
    }

    private boolean isBoxInAttackRange(AxisAlignedBB axisAlignedBB) {
        return RotationUtil.distanceToBox(axisAlignedBB) <= (double)((Float)this.attackRange.getValue()).floatValue();
    }

    private boolean isPlayerTarget(EntityLivingBase entityLivingBase) {
        return entityLivingBase instanceof EntityPlayer && TeamUtil.isTarget((EntityPlayer)entityLivingBase);
    }

    private int findEmptySlot(int currentSlot) {
        int i;
        for (i = 0; i < 9; ++i) {
            if (i == currentSlot || KillAura.mc.field_71439_g.field_71071_by.func_70301_a(i) != null) continue;
            return i;
        }
        for (i = 0; i < 9; ++i) {
            ItemStack stack;
            if (i == currentSlot || (stack = KillAura.mc.field_71439_g.field_71071_by.func_70301_a(i)) == null || stack.func_82837_s()) continue;
            return i;
        }
        return Math.floorMod(currentSlot - 1, 9);
    }

    private int findSwordSlot(int currentSlot) {
        for (int i = 0; i < 9; ++i) {
            ItemStack item;
            if (i == currentSlot || (item = KillAura.mc.field_71439_g.field_71071_by.func_70301_a(i)) == null || !(item.func_77973_b() instanceof ItemSword)) continue;
            return i;
        }
        return -1;
    }

    public KillAura() {
        super("KillAura", false);
    }

    public EntityLivingBase getTarget() {
        return this.target != null ? this.target.getEntity() : null;
    }

    public boolean isAttackAllowed() {
        Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled()) {
            return false;
        }
        if (!((Boolean)this.weaponsOnly.getValue()).booleanValue() || ItemUtil.hasRawUnbreakingEnchant() || ((Boolean)this.allowTools.getValue()).booleanValue() && ItemUtil.isHoldingTool()) {
            return (Boolean)this.requirePress.getValue() == false || KeyBindUtil.isKeyDown(KillAura.mc.field_71474_y.field_74312_F.func_151463_i());
        }
        return false;
    }

    public boolean shouldAutoBlock() {
        if (this.isPlayerBlocking() && this.isBlocking) {
            return !KillAura.mc.field_71439_g.func_70090_H() && !KillAura.mc.field_71439_g.func_180799_ab() && ((Integer)this.autoBlock.getValue() == 3 || (Integer)this.autoBlock.getValue() == 4 || (Integer)this.autoBlock.getValue() == 5 || (Integer)this.autoBlock.getValue() == 6 || (Integer)this.autoBlock.getValue() == 7);
        }
        return false;
    }

    public boolean isBlocking() {
        return this.fakeBlockState && ItemUtil.isHoldingSword();
    }

    public boolean isPlayerBlocking() {
        return (KillAura.mc.field_71439_g.func_71039_bw() || this.blockingState) && ItemUtil.isHoldingSword();
    }

    @EventTarget(value=3)
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.POST && this.blinkReset) {
            this.blinkReset = false;
            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
            Myau.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
        }
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            boolean block;
            if (this.attackDelayMS > 0L) {
                this.attackDelayMS -= 50L;
            }
            boolean attack = this.target != null && this.canAttack();
            boolean bl = block = attack && this.canAutoBlock();
            if (!block) {
                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                this.isBlocking = false;
                this.fakeBlockState = false;
                this.blockTick = 0;
            }
            if (attack) {
                boolean swap = false;
                boolean blocked = false;
                if (block) {
                    switch ((Integer)this.autoBlock.getValue()) {
                        case 0: {
                            if (PlayerUtil.isUsingItem()) {
                                this.isBlocking = true;
                                if (!(this.isPlayerBlocking() || Myau.playerStateManager.digging || Myau.playerStateManager.placing)) {
                                    swap = true;
                                }
                            } else {
                                this.isBlocking = false;
                                if (this.isPlayerBlocking() && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    this.stopBlock();
                                }
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.fakeBlockState = false;
                            break;
                        }
                        case 1: {
                            if (this.hasValidTarget()) {
                                if (!(this.isPlayerBlocking() || Myau.playerStateManager.digging || Myau.playerStateManager.placing)) {
                                    swap = true;
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 2: {
                            int slot;
                            int item;
                            if (this.hasValidTarget()) {
                                item = ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).getCurrentPlayerItem();
                                if (Myau.playerStateManager.digging || Myau.playerStateManager.placing || KillAura.mc.field_71439_g.field_71071_by.field_70461_c != item || this.isPlayerBlocking() && this.blockTick != 0 || this.attackDelayMS > 0L && this.attackDelayMS <= 50L) {
                                    this.blockTick = 0;
                                } else {
                                    slot = this.findEmptySlot(item);
                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(item));
                                    swap = true;
                                    this.blockTick = 1;
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 3: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blinkReset = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 80L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 4: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blinkReset = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 5: {
                            int slot;
                            int item;
                            if (this.hasValidTarget()) {
                                item = ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).getCurrentPlayerItem();
                                if (KillAura.mc.field_71439_g.field_71071_by.field_70461_c == item && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blinkReset = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                slot = this.findEmptySlot(item);
                                                PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                                                ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).setCurrentPlayerItem(slot);
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 6: {
                            int slot;
                            int item;
                            if (this.hasValidTarget() && KillAura.mc.field_71439_g.field_71071_by.field_70461_c == (item = ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).getCurrentPlayerItem()) && !Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                switch (this.blockTick) {
                                    case 0: {
                                        slot = this.findSwordSlot(item);
                                        if (slot == -1) break;
                                        if (!this.isPlayerBlocking()) {
                                            swap = true;
                                        }
                                        this.blockTick = 1;
                                        break;
                                    }
                                    case 1: {
                                        int swordsSlot = this.findSwordSlot(item);
                                        if (swordsSlot == -1) {
                                            this.blockTick = 0;
                                            break;
                                        }
                                        if (!this.isPlayerBlocking()) {
                                            swap = true;
                                            break;
                                        }
                                        if (this.attackDelayMS > 50L) break;
                                        PacketUtil.sendPacket(new C09PacketHeldItemChange(swordsSlot));
                                        ((IAccessorPlayerControllerMP)KillAura.mc.field_71442_b).setCurrentPlayerItem(swordsSlot);
                                        this.startBlock(KillAura.mc.field_71439_g.field_71071_by.func_70301_a(swordsSlot));
                                        attack = false;
                                        this.blockTick = 0;
                                        break;
                                    }
                                    default: {
                                        this.blockTick = 0;
                                    }
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 7: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                        }
                                    }
                                }
                                Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                                this.isBlocking = true;
                                this.fakeBlockState = false;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 8: {
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = this.hasValidTarget();
                            if (!PlayerUtil.isUsingItem() || this.isPlayerBlocking() || Myau.playerStateManager.digging || Myau.playerStateManager.placing) break;
                            swap = true;
                            break;
                        }
                        case 9: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            this.setCurrentSlot();
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            blocked = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            this.stopBlock();
                                            attack = false;
                                            this.setNextSlot();
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                            this.setCurrentSlot();
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            if (this.blockTick == 1 && this.isPlayerBlocking()) {
                                this.stopBlock();
                                this.setNextSlot();
                            }
                            this.blockTick = 0;
                            this.setCurrentSlot();
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 10: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            this.setCurrentSlot();
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            blocked = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                            }
                                            attack = false;
                                            int emptySlot = this.findEmptySlot(KillAura.mc.field_71439_g.field_71071_by.field_70461_c);
                                            PacketUtil.sendPacket(new C09PacketHeldItemChange(emptySlot));
                                            this.swapped = true;
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                            this.setCurrentSlot();
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            if (this.blockTick == 1 && this.isPlayerBlocking()) {
                                this.stopBlock();
                                this.setCurrentSlot();
                            }
                            this.blockTick = 0;
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 11: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            blocked = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                if (Myau.moduleManager.modules.get(NoSlow.class).isEnabled()) {
                                                    int randomSlot = new Random().nextInt(9);
                                                    while (randomSlot == KillAura.mc.field_71439_g.field_71071_by.field_70461_c) {
                                                        randomSlot = new Random().nextInt(9);
                                                    }
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(randomSlot));
                                                    PacketUtil.sendPacket(new C09PacketHeldItemChange(KillAura.mc.field_71439_g.field_71071_by.field_70461_c));
                                                }
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 50L) break;
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                            break;
                        }
                        case 12: {
                            if (this.hasValidTarget()) {
                                if (!Myau.playerStateManager.digging && !Myau.playerStateManager.placing) {
                                    switch (this.blockTick) {
                                        case 0: {
                                            this.setCurrentSlot();
                                            if (!this.isPlayerBlocking()) {
                                                swap = true;
                                            }
                                            blocked = true;
                                            this.blockTick = 1;
                                            break;
                                        }
                                        case 1: {
                                            if (this.isPlayerBlocking()) {
                                                this.stopBlock();
                                                attack = false;
                                            }
                                            if (this.attackDelayMS > 50L) break;
                                            this.setNextSlot();
                                            this.blockTick = 0;
                                            break;
                                        }
                                        default: {
                                            this.blockTick = 0;
                                            this.setCurrentSlot();
                                        }
                                    }
                                }
                                this.isBlocking = true;
                                this.fakeBlockState = true;
                                break;
                            }
                            if (this.blockTick == 1 && this.isPlayerBlocking()) {
                                this.stopBlock();
                                this.setNextSlot();
                            }
                            this.blockTick = 0;
                            this.setCurrentSlot();
                            Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                            this.isBlocking = false;
                            this.fakeBlockState = false;
                        }
                    }
                }
                boolean attacked = false;
                if (this.isBoxInSwingRange(this.target.getBox())) {
                    if ((Integer)this.rotations.getValue() == 2 || (Integer)this.rotations.getValue() == 3) {
                        float[] rotations = RotationUtil.getRotationsToBox(this.target.getBox(), event.getYaw(), event.getPitch(), (float)((Integer)this.angleStep.getValue()).intValue() + RandomUtil.nextFloat(-5.0f, 5.0f), (float)((Integer)this.smoothing.getValue()).intValue() / 100.0f);
                        event.setRotation(rotations[0], rotations[1], 1);
                        if ((Integer)this.rotations.getValue() == 3) {
                            Myau.rotationManager.setRotation(rotations[0], rotations[1], 1, true);
                        }
                        if ((Integer)this.moveFix.getValue() != 0 || (Integer)this.rotations.getValue() == 3) {
                            event.setPervRotation(rotations[0], 1);
                        }
                    }
                    if (attack) {
                        attacked = this.performAttack(event.getNewYaw(), event.getNewPitch());
                    }
                }
                if (swap) {
                    if (attacked) {
                        this.interactAttack(event.getNewYaw(), event.getNewPitch());
                    } else {
                        this.sendUseItem();
                    }
                }
                if (blocked) {
                    Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
                    Myau.blinkManager.setBlinkState(true, BlinkModules.AUTO_BLOCK);
                }
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE: {
                    if (this.target == null || !this.isValidTarget(this.target.getEntity()) || !this.isBoxInAttackRange(this.target.getBox()) || !this.isBoxInSwingRange(this.target.getBox()) || this.timer.hasTimeElapsed(((Integer)this.switchDelay.getValue()).longValue())) {
                        this.timer.reset();
                        ArrayList<EntityLivingBase> targets = new ArrayList<EntityLivingBase>();
                        for (Entity entity : KillAura.mc.field_71441_e.field_72996_f) {
                            if (!(entity instanceof EntityLivingBase) || !this.isValidTarget((EntityLivingBase)entity) || !this.isInRange((EntityLivingBase)entity)) continue;
                            targets.add((EntityLivingBase)entity);
                        }
                        if (targets.isEmpty()) {
                            this.target = null;
                        } else {
                            if (targets.stream().anyMatch(this::isInSwingRange)) {
                                targets.removeIf(entityLivingBase -> !this.isInSwingRange((EntityLivingBase)entityLivingBase));
                            }
                            if (targets.stream().anyMatch(this::isInAttackRange)) {
                                targets.removeIf(entityLivingBase -> !this.isInAttackRange((EntityLivingBase)entityLivingBase));
                            }
                            if (targets.stream().anyMatch(this::isPlayerTarget)) {
                                targets.removeIf(entityLivingBase -> !this.isPlayerTarget((EntityLivingBase)entityLivingBase));
                            }
                            targets.sort((entityLivingBase1, entityLivingBase2) -> {
                                int sortBase = 0;
                                switch ((Integer)this.sort.getValue()) {
                                    case 1: {
                                        sortBase = Float.compare(TeamUtil.getHealthScore(entityLivingBase1), TeamUtil.getHealthScore(entityLivingBase2));
                                        break;
                                    }
                                    case 2: {
                                        sortBase = Integer.compare(entityLivingBase1.field_70172_ad, entityLivingBase2.field_70172_ad);
                                        break;
                                    }
                                    case 3: {
                                        sortBase = Float.compare(RotationUtil.angleToEntity((Entity)entityLivingBase1), RotationUtil.angleToEntity((Entity)entityLivingBase2));
                                    }
                                }
                                return sortBase != 0 ? sortBase : Double.compare(RotationUtil.distanceToEntity((Entity)entityLivingBase1), RotationUtil.distanceToEntity((Entity)entityLivingBase2));
                            });
                            if ((Integer)this.mode.getValue() == 1 && this.hitRegistered) {
                                this.hitRegistered = false;
                                ++this.switchTick;
                            }
                            if ((Integer)this.mode.getValue() == 0 || this.switchTick >= targets.size()) {
                                this.switchTick = 0;
                            }
                            this.target = new AttackData((EntityLivingBase)targets.get(this.switchTick));
                        }
                    }
                    if (this.target == null) break;
                    this.target = new AttackData(this.target.getEntity());
                    break;
                }
                case POST: {
                    if (!this.isPlayerBlocking() || KillAura.mc.field_71439_g.func_70632_aY()) break;
                    KillAura.mc.field_71439_g.func_71008_a(KillAura.mc.field_71439_g.func_70694_bm(), KillAura.mc.field_71439_g.func_70694_bm().func_77988_m());
                }
            }
        }
    }

    @EventTarget(value=4)
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && !event.isCancelled()) {
            C07PacketPlayerDigging packet;
            if (event.getPacket() instanceof C07PacketPlayerDigging && (packet = (C07PacketPlayerDigging)event.getPacket()).func_180762_c() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                this.blockingState = false;
            }
            if (event.getPacket() instanceof C09PacketHeldItemChange) {
                this.blockingState = false;
                if (this.isBlocking) {
                    KillAura.mc.field_71439_g.func_71034_by();
                }
            }
            if ((Integer)this.debugLog.getValue() == 1 && this.isAttackAllowed()) {
                if (event.getPacket() instanceof S06PacketUpdateHealth && (packet = ((S06PacketUpdateHealth)event.getPacket()).func_149332_c() - KillAura.mc.field_71439_g.func_110143_aJ()) != 0.0f && this.lastTickProcessed != KillAura.mc.field_71439_g.field_70173_aa) {
                    this.lastTickProcessed = KillAura.mc.field_71439_g.field_70173_aa;
                    ChatUtil.sendFormatted(String.format("%sHealth: %s&l%s&r (&otick: %d&r)&r", Myau.clientName, packet > 0.0f ? "&a" : "&c", df.format(packet), KillAura.mc.field_71439_g.field_70173_aa));
                }
                if (event.getPacket() instanceof S1CPacketEntityMetadata && (packet = (S1CPacketEntityMetadata)event.getPacket()).func_149375_d() == KillAura.mc.field_71439_g.func_145782_y()) {
                    for (DataWatcher.WatchableObject watchableObject : packet.func_149376_c()) {
                        float diff;
                        if (watchableObject.func_75672_a() != 6 || (diff = ((Float)watchableObject.func_75669_b()).floatValue() - KillAura.mc.field_71439_g.func_110143_aJ()) == 0.0f || this.lastTickProcessed == KillAura.mc.field_71439_g.field_70173_aa) continue;
                        this.lastTickProcessed = KillAura.mc.field_71439_g.field_70173_aa;
                        ChatUtil.sendFormatted(String.format("%sHealth: %s&l%s&r (&otick: %d&r)&r", Myau.clientName, diff > 0.0f ? "&a" : "&c", df.format(diff), KillAura.mc.field_71439_g.field_70173_aa));
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled()) {
            if ((Integer)this.moveFix.getValue() == 1 && (Integer)this.rotations.getValue() != 3 && RotationState.isActived() && RotationState.getPriority() == 1.0f && MoveUtil.isForwardPressed()) {
                MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
            }
            if (this.shouldAutoBlock()) {
                KillAura.mc.field_71439_g.field_71158_b.field_78901_c = false;
            }
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled() && this.target != null && (Integer)this.showTarget.getValue() != 0 && TeamUtil.isEntityLoaded((Entity)this.target.getEntity()) && this.isAttackAllowed()) {
            Color color = new Color(-1);
            switch ((Integer)this.showTarget.getValue()) {
                case 1: {
                    if (this.target.getEntity().field_70737_aN > 0) {
                        color = new Color(0xFF5555);
                        break;
                    }
                    color = new Color(0x55FF55);
                    break;
                }
                case 2: {
                    color = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis());
                }
            }
            RenderUtil.enableRenderState();
            RenderUtil.drawEntityBox((Entity)this.target.getEntity(), color.getRed(), color.getGreen(), color.getBlue());
            RenderUtil.disableRenderState();
        }
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else if (this.isEnabled() && this.target != null && this.canAttack()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else if (this.isEnabled() && this.target != null && this.canAttack()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        } else if (this.isEnabled() && this.target != null && this.canAttack()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onCancelUse(CancelUseEvent event) {
        if (this.isBlocking) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnabled() {
        this.target = null;
        this.switchTick = 0;
        this.hitRegistered = false;
        this.attackDelayMS = 0L;
        this.blockTick = 0;
        this.swapped = false;
    }

    @Override
    public void onDisabled() {
        Myau.blinkManager.setBlinkState(false, BlinkModules.AUTO_BLOCK);
        this.blockingState = false;
        this.isBlocking = false;
        this.fakeBlockState = false;
        this.swapped = false;
    }

    @Override
    public void verifyValue(String mode) {
        if (!this.autoBlock.getName().equals(mode) && !this.autoBlockCPS.getName().equals(mode)) {
            if (this.swingRange.getName().equals(mode)) {
                if (((Float)this.swingRange.getValue()).floatValue() < ((Float)this.attackRange.getValue()).floatValue()) {
                    this.attackRange.setValue(this.swingRange.getValue());
                }
            } else if (this.attackRange.getName().equals(mode)) {
                if (((Float)this.swingRange.getValue()).floatValue() < ((Float)this.attackRange.getValue()).floatValue()) {
                    this.swingRange.setValue(this.attackRange.getValue());
                }
            } else if (this.minCPS.getName().equals(mode)) {
                if ((Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
                    this.maxCPS.setValue(this.minCPS.getValue());
                }
            } else if (this.maxCPS.getName().equals(mode) && (Integer)this.minCPS.getValue() > (Integer)this.maxCPS.getValue()) {
                this.minCPS.setValue(this.maxCPS.getValue());
            }
        } else {
            boolean badCps;
            boolean bl = badCps = (Integer)this.autoBlock.getValue() >= 2 && (Integer)this.autoBlock.getValue() <= 11;
            if (badCps && ((Float)this.autoBlockCPS.getValue()).floatValue() > 10.0f) {
                this.autoBlockCPS.setValue(Float.valueOf(10.0f));
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }

    public boolean hasTarget() {
        return this.target != null;
    }

    private void setNextSlot() {
        int bestSwapSlot = this.getNextSlot();
        PacketUtil.sendPacket(new C09PacketHeldItemChange(bestSwapSlot));
        this.swapped = true;
    }

    private void setCurrentSlot() {
        if (!this.swapped) {
            return;
        }
        PacketUtil.sendPacket(new C09PacketHeldItemChange(KillAura.mc.field_71439_g.field_71071_by.field_70461_c));
        this.swapped = false;
    }

    private int getNextSlot() {
        int currentSlot = KillAura.mc.field_71439_g.field_71071_by.field_70461_c;
        if (currentSlot < 8) {
            return currentSlot + 1;
        }
        return currentSlot - 1;
    }

    public static class AttackData {
        private final EntityLivingBase entity;
        private final AxisAlignedBB box;
        private final double x;
        private final double y;
        private final double z;

        public AttackData(EntityLivingBase entityLivingBase) {
            this.entity = entityLivingBase;
            double collisionBorderSize = entityLivingBase.func_70111_Y();
            this.box = entityLivingBase.func_174813_aQ().func_72314_b(collisionBorderSize, collisionBorderSize, collisionBorderSize);
            this.x = entityLivingBase.field_70165_t;
            this.y = entityLivingBase.field_70163_u;
            this.z = entityLivingBase.field_70161_v;
        }

        public EntityLivingBase getEntity() {
            return this.entity;
        }

        public AxisAlignedBB getBox() {
            return this.box;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}
