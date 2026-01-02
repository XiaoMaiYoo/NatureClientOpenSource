package myau.mixin;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(value=Side.CLIENT)
@Mixin(value={RenderManager.class})
public interface IAccessorRenderManager {
    @Accessor
    public double getRenderPosX();

    @Accessor
    public double getRenderPosY();

    @Accessor
    public double getRenderPosZ();
}
