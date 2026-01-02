package myau.util;

import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;

public class ServerUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static ArrayList<String> getScoreboardLines() {
        if (ServerUtil.mc.field_71441_e == null) {
            return new ArrayList<String>();
        }
        Scoreboard scoreboard = ServerUtil.mc.field_71441_e.func_96441_U();
        if (scoreboard == null) {
            return new ArrayList<String>();
        }
        ScoreObjective scoreObjective = scoreboard.func_96539_a(1);
        if (scoreObjective == null) {
            return new ArrayList<String>();
        }
        return (ArrayList)scoreboard.func_96534_i(scoreObjective).stream().map(score -> ScorePlayerTeam.func_96667_a((Team)scoreboard.func_96509_i(score.func_96653_e()), (String)score.func_96653_e())).collect(Collectors.toList());
    }

    public static boolean isHypixel() {
        ArrayList<String> arrayList = ServerUtil.getScoreboardLines();
        if (arrayList.isEmpty()) {
            return false;
        }
        if (arrayList.get(0).equals("\u00a7ewww.hypixel.ne\ud83c\udf82\u00a7et")) {
            return true;
        }
        return arrayList.get(0).equals("\u00a7ewww.hypixel.ne\u00a7g\u00a7et");
    }

    public static boolean hasPlayerCountInfo() {
        for (String s : ServerUtil.getScoreboardLines()) {
            if (!s.matches(".*Players: \u00a7a\\d+/\\d+.*")) continue;
            return true;
        }
        return false;
    }
}
