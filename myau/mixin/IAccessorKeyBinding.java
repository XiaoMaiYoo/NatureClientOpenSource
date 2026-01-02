package myau.mixin;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={KeyBinding.class})
public interface IAccessorKeyBinding {
    @Accessor(value="pressed")
    public void setPressed(boolean var1);
}
