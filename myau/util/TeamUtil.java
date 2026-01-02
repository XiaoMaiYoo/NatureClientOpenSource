package myau.util;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import myau.Myau;
import myau.util.ServerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class TeamUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static boolean isEntityLoaded(Entity entity) {
        if (entity == null) {
            return false;
        }
        return TeamUtil.mc.field_71441_e.field_72996_f.contains(entity);
    }

    public static List<Entity> getLoadedEntitiesSorted() {
        return TeamUtil.mc.field_71441_e.field_72996_f.stream().sorted((entity1, entity2) -> {
            double dist2;
            double dist1 = mc.func_175598_ae().func_78714_a(entity1.field_70165_t, entity1.field_70163_u, entity1.field_70161_v);
            if (dist1 < (dist2 = mc.func_175598_ae().func_78714_a(entity2.field_70165_t, entity2.field_70163_u, entity2.field_70161_v))) {
                return 1;
            }
            if (dist1 > dist2) {
                return -1;
            }
            return entity1.func_110124_au().toString().compareTo(entity2.func_110124_au().toString());
        }).collect(Collectors.toList());
    }

    public static float getHealthScore(EntityLivingBase entityLivingBase) {
        return entityLivingBase.func_110143_aJ() * (20.0f / (float)entityLivingBase.func_70658_aO());
    }

    public static String stripName(Entity entity) {
        return entity.func_145748_c_().func_150254_d().replaceAll("\u00a7\\S$", "").replaceAll("(?i)\u00a7r", "\u00a7f").trim();
    }

    public static Color getTeamColor(EntityPlayer player, float alpha) {
        String colorPrefix;
        int colorCode = 0;
        ScorePlayerTeam playerTeam = (ScorePlayerTeam)player.func_96124_cp();
        if (playerTeam != null && (colorPrefix = FontRenderer.func_78282_e((String)playerTeam.func_96668_e())).length() >= 2) {
            colorCode = TeamUtil.mc.field_71466_p.func_175064_b(colorPrefix.charAt(1));
        }
        return new Color((float)(colorCode >> 16 & 0xFF) / 255.0f, (float)(colorCode >> 8 & 0xFF) / 255.0f, (float)(colorCode & 0xFF) / 255.0f, alpha);
    }

    public static boolean isBot(EntityPlayer player) {
        if (player == TeamUtil.mc.field_71439_g) {
            return false;
        }
        NetworkPlayerInfo playerInfo = mc.func_147114_u().func_175104_a(player.func_70005_c_());
        if (playerInfo == null) {
            return true;
        }
        if (!ServerUtil.isHypixel()) {
            return false;
        }
        if (player.func_70005_c_().startsWith("\u00a7k")) {
            return player.func_82150_aj();
        }
        if (playerInfo.func_178853_c() < 1) {
            return true;
        }
        ScorePlayerTeam playerTeam = playerInfo.func_178850_i();
        if (playerTeam == null) {
            return false;
        }
        if (!playerTeam.func_96669_c().isEmpty()) {
            return false;
        }
        return playerTeam.func_96668_e().equals("\u00a7c");
    }

    public static boolean isSameTeam(EntityPlayer player) {
        if (player == TeamUtil.mc.field_71439_g) {
            return true;
        }
        NetworkPlayerInfo selfInfo = mc.func_147114_u().func_175102_a(TeamUtil.mc.field_71439_g.func_110124_au());
        if (selfInfo == null) {
            return false;
        }
        ScorePlayerTeam selfTeam = selfInfo.func_178850_i();
        if (selfTeam == null) {
            return false;
        }
        NetworkPlayerInfo targetInfo = mc.func_147114_u().func_175102_a(player.func_110124_au());
        if (targetInfo == null) {
            return false;
        }
        ScorePlayerTeam targetTeam = targetInfo.func_178850_i();
        if (targetTeam == null) {
            return false;
        }
        return selfTeam.func_96668_e().equals(targetTeam.func_96668_e());
    }

    public static boolean hasTeamColor(EntityLivingBase entity) {
        if (entity == TeamUtil.mc.field_71439_g) {
            return true;
        }
        NetworkPlayerInfo selfInfo = mc.func_147114_u().func_175102_a(TeamUtil.mc.field_71439_g.func_110124_au());
        if (selfInfo == null) {
            return false;
        }
        ScorePlayerTeam selfTeam = selfInfo.func_178850_i();
        if (selfTeam == null) {
            return false;
        }
        if (selfTeam.func_96668_e().length() < 2) {
            return false;
        }
        EntityLivingBase nearestArmorStand = (EntityLivingBase)TeamUtil.mc.field_71441_e.func_72857_a(EntityArmorStand.class, entity.func_174813_aQ(), (Entity)entity);
        if (nearestArmorStand != null) {
            return nearestArmorStand.func_70005_c_().contains(selfTeam.func_96668_e().substring(0, 2));
        }
        return false;
    }

    public static boolean isShop(EntityLivingBase entity) {
        if (entity == TeamUtil.mc.field_71439_g) {
            return false;
        }
        EntityLivingBase armorStand = (EntityLivingBase)TeamUtil.mc.field_71441_e.func_72857_a(EntityArmorStand.class, entity.func_174813_aQ(), (Entity)entity);
        if (armorStand == null) {
            return false;
        }
        String displayName = armorStand.func_70005_c_();
        if (displayName.contains("RIGHT CLICK")) {
            return true;
        }
        if (displayName.contains("ITEM SHOP")) {
            return true;
        }
        if (displayName.contains("UPGRADES")) {
            return true;
        }
        if (displayName.contains("BANKER")) {
            return true;
        }
        return displayName.contains("STREAK POWERS");
    }

    public static boolean isFriend(EntityPlayer player) {
        return Myau.friendManager.isFriend(player.func_70005_c_());
    }

    public static boolean isTarget(EntityPlayer player) {
        return Myau.targetManager.isFriend(player.func_70005_c_());
    }
}
