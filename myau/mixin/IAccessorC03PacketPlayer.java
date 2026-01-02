package myau.mixin;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(value=Side.CLIENT)
@Mixin(value={C03PacketPlayer.class})
public interface IAccessorC03PacketPlayer {
    @Accessor(value="onGround")
    public void setOnGround(boolean var1);
}
