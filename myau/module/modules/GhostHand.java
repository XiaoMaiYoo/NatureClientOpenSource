package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.util.ItemUtil;
import myau.util.TeamUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class GhostHand
extends Module {
    public final BooleanProperty teamsOnly = new BooleanProperty("team-only", true);
    public final BooleanProperty ignoreWeapons = new BooleanProperty("ignore-weapons", false);

    public GhostHand() {
        super("GhostHand", false);
    }

    public boolean shouldSkip(Entity entity) {
        return !(!(entity instanceof EntityPlayer) || TeamUtil.isBot((EntityPlayer)entity) || (Boolean)this.teamsOnly.getValue() != false && !TeamUtil.isSameTeam((EntityPlayer)entity) || (Boolean)this.ignoreWeapons.getValue() != false && ItemUtil.hasRawUnbreakingEnchant());
    }
}
