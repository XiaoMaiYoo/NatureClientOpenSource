package myau.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityPlayer.class})
public interface IAccessorEntityPlayer {
    @Accessor
    public ItemStack getItemInUse();

    @Accessor
    public void setItemInUse(ItemStack var1);

    @Accessor
    public int getItemInUseCount();

    @Accessor
    public void setItemInUseCount(int var1);
}
