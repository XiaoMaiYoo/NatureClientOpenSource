package myau.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@SideOnly(value=Side.CLIENT)
@Mixin(value={PlayerControllerMP.class})
public interface IAccessorPlayerControllerMP {
    @Accessor
    public float getCurBlockDamageMP();

    @Accessor
    public void setCurBlockDamageMP(float var1);

    @Accessor
    public int getBlockHitDelay();

    @Accessor
    public void setBlockHitDelay(int var1);

    @Accessor
    public boolean getIsHittingBlock();

    @Accessor
    public int getCurrentPlayerItem();

    @Accessor
    public void setCurrentPlayerItem(int var1);

    @Invoker
    public void callSyncCurrentPlayItem();
}
