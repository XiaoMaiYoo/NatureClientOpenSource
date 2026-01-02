package myau.mixin;

import myau.Myau;
import myau.event.EventManager;
import myau.events.Render2DEvent;
import myau.events.Shader2DEvent;
import myau.module.modules.NickHider;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiIngameForge.class})
public abstract class MixinGuiIngameForge {
    @Inject(method={"renderGameOverlay"}, at={@At(value="INVOKE", target="Lnet/minecraftforge/client/GuiIngameForge;renderTitle(IIF)V", shift=At.Shift.AFTER, remap=false)})
    private void renderGameOverlay(float partialTicks, CallbackInfo ci) {
        EventManager.call(new Shader2DEvent(Shader2DEvent.ShaderType.SHADOW));
        EventManager.call(new Render2DEvent(partialTicks));
    }

    @Redirect(method={"renderExperience"}, at=@At(value="FIELD", target="Lnet/minecraft/client/entity/EntityPlayerSP;experience:F"))
    private float renderExperience(EntityPlayerSP entityPlayerSP) {
        if (Myau.moduleManager == null) {
            return entityPlayerSP.field_71106_cc;
        }
        NickHider event = (NickHider)Myau.moduleManager.modules.get(NickHider.class);
        return event.isEnabled() && (Boolean)event.level.getValue() != false ? 0.0f : entityPlayerSP.field_71106_cc;
    }

    @Redirect(method={"renderExperience"}, at=@At(value="FIELD", target="Lnet/minecraft/client/entity/EntityPlayerSP;experienceLevel:I"))
    private int renderExperienceLevel(EntityPlayerSP entityPlayerSP) {
        if (Myau.moduleManager == null) {
            return entityPlayerSP.field_71068_ca;
        }
        NickHider event = (NickHider)Myau.moduleManager.modules.get(NickHider.class);
        return event.isEnabled() && (Boolean)event.level.getValue() != false ? 0 : entityPlayerSP.field_71068_ca;
    }
}
