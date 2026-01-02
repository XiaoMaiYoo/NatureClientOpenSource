package myau.module.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import myau.event.EventTarget;
import myau.events.AttackEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class MoreKB
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"LEGIT", "LEGITFAST", "LESSPACKET", "PACKET", "DOUBLEPACKET"});
    public final BooleanProperty intelligent = new BooleanProperty("intelligent", false);
    public final BooleanProperty onlyGround = new BooleanProperty("only-ground", true);
    public final BooleanProperty showKBInfo = new BooleanProperty("ShowKBInfo", false);
    private EntityLivingBase target = null;
    private final Map<UUID, Vec3> prevMotionMap = new HashMap<UUID, Vec3>();

    public MoreKB() {
        super("MoreKB", false);
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Entity targetEntity = event.getTarget();
        if (targetEntity instanceof EntityLivingBase) {
            this.target = (EntityLivingBase)targetEntity;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        for (Object obj : MoreKB.mc.field_71441_e.field_72996_f) {
            if (!(obj instanceof EntityLivingBase)) continue;
            EntityLivingBase entity = (EntityLivingBase)obj;
            this.prevMotionMap.put(entity.func_110124_au(), new Vec3(entity.field_70159_w, entity.field_70181_x, entity.field_70179_y));
        }
        if ((Integer)this.mode.getValue() == 1) {
            if (this.target != null && this.isMoving()) {
                if (!((Boolean)this.onlyGround.getValue()).booleanValue() || MoreKB.mc.field_71439_g.field_70122_E) {
                    MoreKB.mc.field_71439_g.field_71157_e = 0;
                }
                this.target = null;
            }
            return;
        }
        EntityLivingBase entity = null;
        if (MoreKB.mc.field_71476_x != null && MoreKB.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.ENTITY && MoreKB.mc.field_71476_x.field_72308_g instanceof EntityLivingBase) {
            entity = (EntityLivingBase)MoreKB.mc.field_71476_x.field_72308_g;
        }
        if (entity == null) {
            return;
        }
        double x = MoreKB.mc.field_71439_g.field_70165_t - entity.field_70165_t;
        double z = MoreKB.mc.field_71439_g.field_70161_v - entity.field_70161_v;
        float calcYaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI - 90.0);
        float diffY = Math.abs(MathHelper.func_76142_g((float)(calcYaw - entity.field_70177_z)));
        if (((Boolean)this.intelligent.getValue()).booleanValue() && diffY > 120.0f) {
            return;
        }
        if (entity.field_70737_aN == 10) {
            this.executeKB(entity);
            if (((Boolean)this.showKBInfo.getValue()).booleanValue()) {
                this.displayKBInfo(entity);
            }
        }
    }

    private void executeKB(EntityLivingBase entity) {
        switch ((Integer)this.mode.getValue()) {
            case 0: {
                if (!MoreKB.mc.field_71439_g.func_70051_ag()) break;
                MoreKB.mc.field_71439_g.func_70031_b(false);
                MoreKB.mc.field_71439_g.func_70031_b(true);
                break;
            }
            case 2: {
                if (MoreKB.mc.field_71439_g.func_70051_ag()) {
                    MoreKB.mc.field_71439_g.func_70031_b(false);
                }
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                MoreKB.mc.field_71439_g.func_70031_b(true);
                break;
            }
            case 3: {
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.STOP_SPRINTING));
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                MoreKB.mc.field_71439_g.func_70031_b(true);
                break;
            }
            case 4: {
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.STOP_SPRINTING));
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.STOP_SPRINTING));
                mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                MoreKB.mc.field_71439_g.func_70031_b(true);
            }
        }
    }

    private void displayKBInfo(EntityLivingBase entity) {
        UUID uuid = entity.func_110124_au();
        Vec3 prevMotion = this.prevMotionMap.getOrDefault(uuid, new Vec3(0.0, 0.0, 0.0));
        double prevSpeed = Math.sqrt(prevMotion.field_72450_a * prevMotion.field_72450_a + prevMotion.field_72449_c * prevMotion.field_72449_c);
        double currentSpeed = Math.sqrt(entity.field_70159_w * entity.field_70159_w + entity.field_70179_y * entity.field_70179_y);
        double estimatedKB = currentSpeed * 10.0;
        String msg = String.format("\u00a7b[MoreKB] \u00a7r%s \u00a77| \u00a7cKB: %.2f \u00a77| \u00a76Pre: %.2f \u00a77| \u00a7aPost: %.2f \u00a77| \u00a7dHurt: %s", entity.func_70005_c_(), estimatedKB, prevSpeed, currentSpeed, entity.field_70737_aN > 0 ? "\u2713" : "\u2717");
        MoreKB.mc.field_71439_g.func_145747_a((IChatComponent)new ChatComponentText(msg));
    }

    private boolean isMoving() {
        return MoreKB.mc.field_71439_g.field_70701_bs != 0.0f || MoreKB.mc.field_71439_g.field_70702_br != 0.0f;
    }

    @Override
    public void onDisabled() {
        this.prevMotionMap.clear();
        super.onDisabled();
    }
}
