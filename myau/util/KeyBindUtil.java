package myau.util;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class KeyBindUtil {
    public static String getKeyName(int keyCode) {
        return keyCode < 0 ? Mouse.getButtonName((int)(keyCode + 100)) : Keyboard.getKeyName((int)keyCode);
    }

    public static boolean isKeyDown(int keyCode) {
        return keyCode < 0 ? Mouse.isButtonDown((int)(keyCode + 100)) : Keyboard.isKeyDown((int)keyCode);
    }

    public static void updateKeyState(int keyCode) {
        KeyBindUtil.setKeyBindState(keyCode, keyCode < 0 ? Mouse.isButtonDown((int)(keyCode + 100)) : Keyboard.isKeyDown((int)keyCode));
    }

    public static void setKeyBindState(int keyCode, boolean pressed) {
        KeyBinding.func_74510_a((int)keyCode, (boolean)pressed);
    }

    public static void pressKeyOnce(int keyCode) {
        KeyBinding.func_74507_a((int)keyCode);
    }
}
