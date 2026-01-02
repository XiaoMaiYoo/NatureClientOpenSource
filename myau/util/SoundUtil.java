package myau.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

public class SoundUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static void playSound(String soundName) {
        SoundHandler soundHandler = mc.func_147118_V();
        if (soundHandler != null) {
            PositionedSoundRecord positionedSoundRecord = PositionedSoundRecord.func_147673_a((ResourceLocation)new ResourceLocation(soundName));
            soundHandler.func_147682_a((ISound)positionedSoundRecord);
        }
    }
}
