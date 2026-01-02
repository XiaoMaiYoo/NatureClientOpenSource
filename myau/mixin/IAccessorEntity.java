package myau.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@SideOnly(value=Side.CLIENT)
@Mixin(value={Entity.class})
public interface IAccessorEntity {
    @Accessor
    public boolean getIsInWeb();

    @Invoker
    public Vec3 callGetVectorForRotation(float var1, float var2);
}
