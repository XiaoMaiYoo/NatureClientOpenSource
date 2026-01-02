package myau.module.modules;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import myau.Myau;
import myau.event.EventTarget;
import myau.events.PacketEvent;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.module.modules.BedNuker;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.ItemUtil;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class LagRange
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int tickIndex = -1;
    private long delayCounter = 0L;
    private boolean hasTarget = false;
    private Vec3 lastPosition = null;
    private Vec3 currentPosition = null;
    public final IntProperty delay = new IntProperty("delay", 150, 0, 1000);
    public final FloatProperty range = new FloatProperty("range", Float.valueOf(10.0f), Float.valueOf(3.0f), Float.valueOf(100.0f));
    public final BooleanProperty weaponsOnly = new BooleanProperty("weapons-only", true);
    public final BooleanProperty allowTools = new BooleanProperty("allow-tools", false, this.weaponsOnly::getValue);
    public final BooleanProperty botCheck = new BooleanProperty("bot-check", true);
    public final BooleanProperty teams = new BooleanProperty("teams", true);
    public final ModeProperty showPosition = new ModeProperty("show-position", 0, new String[]{"NONE", "DEFAULT", "HUD"});

    private boolean isValidTarget(EntityPlayer entityPlayer) {
        if (entityPlayer != LagRange.mc.field_71439_g && entityPlayer != LagRange.mc.field_71439_g.field_70154_o) {
            if (entityPlayer == mc.func_175606_aa() || entityPlayer == LagRange.mc.func_175606_aa().field_70154_o) {
                return false;
            }
            if (entityPlayer.field_70725_aQ > 0) {
                return false;
            }
            if (TeamUtil.isFriend(entityPlayer)) {
                return false;
            }
            return !((Boolean)this.teams.getValue() != false && TeamUtil.isSameTeam(entityPlayer) || (Boolean)this.botCheck.getValue() != false && TeamUtil.isBot(entityPlayer));
        }
        return false;
    }

    private boolean shouldResetOnPacket(Packet<?> packet) {
        if (packet instanceof C02PacketUseEntity) {
            return true;
        }
        if (packet instanceof C07PacketPlayerDigging) {
            return ((C07PacketPlayerDigging)packet).func_180762_c() != C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;
        }
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            ItemStack item = ((C08PacketPlayerBlockPlacement)packet).func_149574_g();
            return item == null || !(item.func_77973_b() instanceof ItemSword);
        }
        return false;
    }

    public LagRange() {
        super("LagRange", false);
    }

    @EventTarget(value=3)
    public void onTick(TickEvent event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE: {
                    Myau.lagManager.setDelay(0);
                    this.hasTarget = false;
                    BedNuker bedNuker = (BedNuker)Myau.moduleManager.modules.get(BedNuker.class);
                    if (!(bedNuker.isEnabled() && bedNuker.isReady() || ((IAccessorPlayerControllerMP)LagRange.mc.field_71442_b).getIsHittingBlock() || LagRange.mc.field_71439_g.func_71039_bw() && !LagRange.mc.field_71439_g.func_70632_aY() || ((Boolean)this.weaponsOnly.getValue()).booleanValue() && !ItemUtil.hasRawUnbreakingEnchant() && (!((Boolean)this.allowTools.getValue()).booleanValue() || !ItemUtil.isHoldingTool()))) {
                        List players = LagRange.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityPlayer).map(entity -> (EntityPlayer)entity).filter(this::isValidTarget).collect(Collectors.toList());
                        if (players.isEmpty()) {
                            this.tickIndex = -1;
                            break;
                        }
                        double height = LagRange.mc.field_71439_g.func_70047_e();
                        Vec3 eyePosition = Myau.lagManager.getLastPosition().func_72441_c(0.0, height, 0.0);
                        Vec3 targetEyePosition = new Vec3(LagRange.mc.field_71439_g.field_70142_S, LagRange.mc.field_71439_g.field_70137_T + height, LagRange.mc.field_71439_g.field_70136_U);
                        Vec3 playerEyePosition = new Vec3(LagRange.mc.field_71439_g.field_70165_t, LagRange.mc.field_71439_g.field_70163_u + height, LagRange.mc.field_71439_g.field_70161_v);
                        for (EntityPlayer player : players) {
                            double distance = RotationUtil.distanceToBox((Entity)player, playerEyePosition);
                            if (distance > (double)((Float)this.range.getValue()).floatValue()) continue;
                            double targetDist = RotationUtil.distanceToBox((Entity)player, targetEyePosition);
                            double eyeDist = RotationUtil.distanceToBox((Entity)player, eyePosition);
                            if (!(distance < targetDist) && !(distance < eyeDist)) continue;
                            if (this.tickIndex < 0) {
                                this.tickIndex = 0;
                                this.delayCounter += (long)((Integer)this.delay.getValue()).intValue();
                                while (this.delayCounter > 0L) {
                                    ++this.tickIndex;
                                    this.delayCounter -= 50L;
                                }
                            }
                            Myau.lagManager.setDelay(this.tickIndex);
                            this.hasTarget = true;
                            return;
                        }
                        break;
                    }
                    this.tickIndex = -1;
                    break;
                }
                case POST: {
                    Vec3 savedPosition = Myau.lagManager.getLastPosition();
                    this.lastPosition = this.currentPosition == null ? savedPosition : this.currentPosition;
                    this.currentPosition = savedPosition;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && this.shouldResetOnPacket(event.getPacket())) {
            Myau.lagManager.setDelay(0);
            this.tickIndex = -1;
        }
    }

    @EventTarget(value=1)
    public void onRender3D(Render3DEvent event) {
        if (this.isEnabled() && (Integer)this.showPosition.getValue() != 0 && LagRange.mc.field_71474_y.field_74320_O != 0 && this.hasTarget && this.lastPosition != null && this.currentPosition != null) {
            Color color = new Color(-1);
            switch ((Integer)this.showPosition.getValue()) {
                case 1: {
                    color = TeamUtil.getTeamColor((EntityPlayer)LagRange.mc.field_71439_g, 1.0f);
                    break;
                }
                case 2: {
                    color = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis());
                }
            }
            double x = RenderUtil.lerpDouble(this.currentPosition.field_72450_a, this.lastPosition.field_72450_a, event.getPartialTicks());
            double y = RenderUtil.lerpDouble(this.currentPosition.field_72448_b, this.lastPosition.field_72448_b, event.getPartialTicks());
            double z = RenderUtil.lerpDouble(this.currentPosition.field_72449_c, this.lastPosition.field_72449_c, event.getPartialTicks());
            float size = LagRange.mc.field_71439_g.func_70111_Y();
            AxisAlignedBB aabb = new AxisAlignedBB(x - (double)LagRange.mc.field_71439_g.field_70130_N / 2.0, y, z - (double)LagRange.mc.field_71439_g.field_70130_N / 2.0, x + (double)LagRange.mc.field_71439_g.field_70130_N / 2.0, y + (double)LagRange.mc.field_71439_g.field_70131_O, z + (double)LagRange.mc.field_71439_g.field_70130_N / 2.0).func_72314_b((double)size, (double)size, (double)size).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ());
            RenderUtil.enableRenderState();
            RenderUtil.drawFilledBox(aabb, color.getRed(), color.getGreen(), color.getBlue());
            RenderUtil.disableRenderState();
        }
    }

    @Override
    public void onDisabled() {
        Myau.lagManager.setDelay(0);
        this.tickIndex = -1;
        this.delayCounter = 0L;
        this.hasTarget = false;
        this.lastPosition = null;
        this.currentPosition = null;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{String.format("%dms", this.delay.getValue())};
    }
}
