package myau.mixin;

import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityLivingBase.class})
public interface IAccessorEntityLivingBase {
    @Accessor
    public Map<Integer, PotionEffect> getActivePotionsMap();

    @Accessor
    public AttributeModifier getSprintingSpeedBoostModifier();

    @Accessor
    public int getJumpTicks();

    @Accessor
    public void setJumpTicks(int var1);
}
