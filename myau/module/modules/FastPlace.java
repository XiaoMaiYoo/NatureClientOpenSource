package myau.module.modules;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.mixin.IAccessorMinecraft;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.util.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class FastPlace
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat df = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.US));
    private long delayMS = 0L;
    public final FloatProperty delay = new FloatProperty("delay", Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(3.0f));
    public final BooleanProperty blocksOnly = new BooleanProperty("blocks-only", true);
    public final BooleanProperty placeFix = new BooleanProperty("place-fix", true);

    private boolean canPlace() {
        ItemStack stack = FastPlace.mc.field_71439_g.func_70694_bm();
        if (stack != null) {
            Item item = stack.func_77973_b();
            if (item instanceof ItemFishingRod) {
                return false;
            }
            if (item instanceof ItemBlock) {
                if (!((Boolean)this.placeFix.getValue()).booleanValue()) {
                    return true;
                }
                MovingObjectPosition mop = RotationUtil.rayTrace(FastPlace.mc.field_71439_g.field_70177_z, FastPlace.mc.field_71439_g.field_70125_A, (double)FastPlace.mc.field_71442_b.func_78757_d(), 1.0f);
                return mop != null && mop.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK && ((ItemBlock)item).func_179222_a((World)FastPlace.mc.field_71441_e, mop.func_178782_a(), mop.field_178784_b, (EntityPlayer)FastPlace.mc.field_71439_g, stack);
            }
        }
        return (Boolean)this.blocksOnly.getValue() == false;
    }

    public FastPlace() {
        super("FastPlace", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            int rightClickDelayTimer = ((IAccessorMinecraft)mc).getRightClickDelayTimer();
            if (rightClickDelayTimer == 4) {
                this.delayMS += (long)(50.0f * ((Float)this.delay.getValue()).floatValue());
            }
            if (this.delayMS > 0L) {
                this.delayMS -= 50L;
            }
            if (this.delayMS <= 0L && rightClickDelayTimer > 1 && this.canPlace()) {
                ((IAccessorMinecraft)mc).setRightClickDelayTimer(0);
            }
        }
    }

    @Override
    public void onDisabled() {
        this.delayMS = 0L;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{df.format(this.delay.getValue())};
    }
}
