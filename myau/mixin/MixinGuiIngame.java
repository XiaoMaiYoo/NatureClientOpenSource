package myau.mixin;

import myau.Myau;
import myau.module.modules.Block;
import myau.module.modules.Scaffold;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiIngame.class})
public abstract class MixinGuiIngame {
    @Redirect(method={"updateTick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack updateTick(InventoryPlayer inventoryPlayer) {
        int slot;
        int slot2;
        Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled() && ((Boolean)scaffold.itemSpoof.getValue()).booleanValue() && (slot2 = scaffold.getSlot()) >= 0) {
            return inventoryPlayer.func_70301_a(slot2);
        }
        Block autoBlockIn = (Block)Myau.moduleManager.modules.get(Block.class);
        if (((Boolean)autoBlockIn.itemSpoof.getValue()).booleanValue() && autoBlockIn.isEnabled() && (slot = autoBlockIn.getSlot()) >= 0) {
            return inventoryPlayer.func_70301_a(slot);
        }
        return inventoryPlayer.func_70448_g();
    }
}
