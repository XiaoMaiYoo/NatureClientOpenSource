package myau.module.modules;

import java.util.regex.Matcher;
import myau.enums.ChatColors;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.TextProperty;
import net.minecraft.client.Minecraft;

public class NickHider
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final TextProperty protectName = new TextProperty("name", "You");
    public final BooleanProperty scoreboard = new BooleanProperty("scoreboard", true);
    public final BooleanProperty level = new BooleanProperty("level", true);

    public NickHider() {
        super("NickHider", false, true);
    }

    public String replaceNick(String input) {
        if (input != null && NickHider.mc.field_71439_g != null) {
            if (((Boolean)this.scoreboard.getValue()).booleanValue() && input.matches("\u00a77\\d{2}/\\d{2}/\\d{2}(?:\\d{2})?  ?\u00a78.*")) {
                input = input.replaceAll("\u00a78", "\u00a78\u00a7k").replaceAll("[^\\x00-\\x7F\u00a7]", "?");
            }
            return input.replaceAll(NickHider.mc.field_71439_g.func_70005_c_(), Matcher.quoteReplacement(ChatColors.formatColor((String)this.protectName.getValue())));
        }
        return input;
    }
}
