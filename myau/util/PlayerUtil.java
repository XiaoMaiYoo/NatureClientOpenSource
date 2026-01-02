package myau.util;

import myau.Myau;
import myau.module.modules.KeepSprint;
import myau.util.BlockUtil;
import myau.util.KeyBindUtil;
import myau.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeHooks;

public class PlayerUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static boolean isJumping() {
        return PlayerUtil.mc.field_71462_r == null && KeyBindUtil.isKeyDown(PlayerUtil.mc.field_71474_y.field_74314_A.func_151463_i());
    }

    public static boolean isSneaking() {
        return PlayerUtil.mc.field_71462_r == null && KeyBindUtil.isKeyDown(PlayerUtil.mc.field_71474_y.field_74311_E.func_151463_i());
    }

    public static boolean isMovingLeft() {
        return PlayerUtil.mc.field_71462_r == null && KeyBindUtil.isKeyDown(PlayerUtil.mc.field_71474_y.field_74370_x.func_151463_i());
    }

    public static boolean isMovingRight() {
        return PlayerUtil.mc.field_71462_r == null && KeyBindUtil.isKeyDown(PlayerUtil.mc.field_71474_y.field_74366_z.func_151463_i());
    }

    public static boolean isAttacking() {
        return PlayerUtil.mc.field_71462_r == null && KeyBindUtil.isKeyDown(PlayerUtil.mc.field_71474_y.field_74312_F.func_151463_i());
    }

    public static boolean isUsingItem() {
        return PlayerUtil.mc.field_71462_r == null && KeyBindUtil.isKeyDown(PlayerUtil.mc.field_71474_y.field_74313_G.func_151463_i());
    }

    public static boolean canFly(float fallThreshold) {
        if (!PlayerUtil.mc.field_71439_g.field_71075_bZ.field_75101_c && !PlayerUtil.mc.field_71439_g.field_71075_bZ.field_75102_a) {
            PotionEffect jumpEffect = PlayerUtil.mc.field_71439_g.func_70660_b(Potion.field_76430_j);
            float jumpBoost = jumpEffect != null ? (float)(jumpEffect.func_76458_c() + 1) : 0.0f;
            float fallDistance = PlayerUtil.mc.field_71439_g.field_70143_R;
            if (PlayerUtil.mc.field_71439_g.field_70181_x < -0.67 || !PlayerUtil.isAirBelow()) {
                fallDistance -= (float)PlayerUtil.mc.field_71439_g.field_70181_x;
            }
            return MathHelper.func_76123_f((float)(fallDistance - fallThreshold - jumpBoost)) > 0;
        }
        return false;
    }

    public static boolean canFly(int checkHeight) {
        if (!PlayerUtil.mc.field_71439_g.field_71075_bZ.field_75101_c && !PlayerUtil.mc.field_71439_g.field_71075_bZ.field_75102_a) {
            int currentY;
            int playerY = MathHelper.func_76128_c((double)PlayerUtil.mc.field_71439_g.field_70163_u);
            for (int offset = 0; offset <= checkHeight && (currentY = playerY - offset) >= 0; ++offset) {
                Block block = PlayerUtil.mc.field_71441_e.func_180495_p(new BlockPos(PlayerUtil.mc.field_71439_g.field_70165_t, (double)currentY, PlayerUtil.mc.field_71439_g.field_70161_v)).func_177230_c();
                if (block instanceof BlockAir) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isInWater() {
        return PlayerUtil.checkInWater(PlayerUtil.mc.field_71439_g.func_174813_aQ().func_72314_b(-1.0E-6, 0.0, -1.0E-6));
    }

    public static boolean checkInWater(AxisAlignedBB boundingBox) {
        if (!PlayerUtil.mc.field_71439_g.func_70090_H() && !PlayerUtil.mc.field_71439_g.func_180799_ab()) {
            int minY = MathHelper.func_76128_c((double)boundingBox.field_72338_b);
            if (minY < 0) {
                return true;
            }
            int minX = MathHelper.func_76128_c((double)boundingBox.field_72340_a);
            int maxX = MathHelper.func_76128_c((double)(boundingBox.field_72336_d + 1.0));
            int minZ = MathHelper.func_76128_c((double)boundingBox.field_72339_c);
            int maxZ = MathHelper.func_76128_c((double)(boundingBox.field_72334_f + 1.0));
            for (int x = minX; x < maxX; ++x) {
                for (int z = minZ; z < maxZ; ++z) {
                    for (int y = minY; y >= 0; --y) {
                        if (BlockUtil.isReplaceable(new BlockPos(x, y, z))) continue;
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean canMove(double x, double z) {
        return PlayerUtil.canMove(x, z, -1.0);
    }

    public static boolean canMove(double x, double z, double y) {
        AxisAlignedBB boundingBox = PlayerUtil.mc.field_71439_g.func_174813_aQ().func_72317_d(x, y, z);
        return PlayerUtil.mc.field_71441_e.func_72945_a((Entity)PlayerUtil.mc.field_71439_g, boundingBox).isEmpty();
    }

    public static boolean isAirBelow() {
        AxisAlignedBB axisAlignedBB = PlayerUtil.mc.field_71439_g.func_174813_aQ().func_72317_d(0.0, -1.0, 0.0);
        return !PlayerUtil.mc.field_71441_e.func_72945_a((Entity)PlayerUtil.mc.field_71439_g, axisAlignedBB).isEmpty();
    }

    public static boolean isAirAbove() {
        AxisAlignedBB axisAlignedBB = PlayerUtil.mc.field_71439_g.func_174813_aQ().func_72317_d(0.0, 1.0, 0.0);
        return !PlayerUtil.mc.field_71441_e.func_72945_a((Entity)PlayerUtil.mc.field_71439_g, axisAlignedBB).isEmpty();
    }

    public static boolean canReach(BlockPos blockPos, double reach) {
        return PlayerUtil.isBlockWithinReach(blockPos, PlayerUtil.mc.field_71439_g.field_70165_t, PlayerUtil.mc.field_71439_g.field_70163_u + (double)PlayerUtil.mc.field_71439_g.func_70047_e(), PlayerUtil.mc.field_71439_g.field_70161_v, reach);
    }

    public static boolean isBlockWithinReach(BlockPos blockPos, double x, double y, double z, double reach) {
        return blockPos.func_177957_d(x, y, z) < Math.pow(reach, 2.0);
    }

    public static void attackEntity(Entity target) {
        if (ForgeHooks.onPlayerAttackTarget((EntityPlayer)PlayerUtil.mc.field_71439_g, (Entity)target) && target.func_70075_an() && !target.func_85031_j((Entity)PlayerUtil.mc.field_71439_g)) {
            float baseDamage = (float)PlayerUtil.mc.field_71439_g.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
            float enchantmentBonus = EnchantmentHelper.func_152377_a((ItemStack)PlayerUtil.mc.field_71439_g.func_70694_bm(), (EnumCreatureAttribute)(target instanceof EntityLivingBase ? ((EntityLivingBase)target).func_70668_bt() : EnumCreatureAttribute.UNDEFINED));
            int knockbackLevel = EnchantmentHelper.func_77501_a((EntityLivingBase)PlayerUtil.mc.field_71439_g);
            if (PlayerUtil.mc.field_71439_g.func_70051_ag()) {
                ++knockbackLevel;
            }
            if (baseDamage > 0.0f || enchantmentBonus > 0.0f) {
                boolean isCritical;
                boolean bl = isCritical = PlayerUtil.mc.field_71439_g.field_70143_R > 0.0f && !PlayerUtil.mc.field_71439_g.field_70122_E && !PlayerUtil.mc.field_71439_g.func_70617_f_() && !PlayerUtil.mc.field_71439_g.func_70090_H() && !PlayerUtil.mc.field_71439_g.func_70644_a(Potion.field_76440_q) && PlayerUtil.mc.field_71439_g.field_70154_o == null;
                if (isCritical && baseDamage > 0.0f) {
                    baseDamage *= 1.5f;
                }
                baseDamage += enchantmentBonus;
                boolean isFireAspectApplied = false;
                int fireAspectLevel = EnchantmentHelper.func_90036_a((EntityLivingBase)PlayerUtil.mc.field_71439_g);
                if (target instanceof EntityLivingBase && fireAspectLevel > 0 && !target.func_70027_ad()) {
                    isFireAspectApplied = true;
                    target.func_70015_d(1);
                }
                double originalMotionX = target.field_70159_w;
                double originalMotionY = target.field_70181_x;
                double originalMotionZ = target.field_70179_y;
                if (target.func_70097_a(DamageSource.func_76365_a((EntityPlayer)PlayerUtil.mc.field_71439_g), baseDamage)) {
                    if (knockbackLevel > 0) {
                        target.func_70024_g((double)(-MathHelper.func_76126_a((float)(PlayerUtil.mc.field_71439_g.field_70177_z * (float)Math.PI / 180.0f)) * (float)knockbackLevel * 0.5f), 0.1, (double)(MathHelper.func_76134_b((float)(PlayerUtil.mc.field_71439_g.field_70177_z * (float)Math.PI / 180.0f)) * (float)knockbackLevel * 0.5f));
                        KeepSprint keepSprint = (KeepSprint)Myau.moduleManager.modules.get(KeepSprint.class);
                        if (!(!keepSprint.isEnabled() || ((Boolean)keepSprint.groundOnly.getValue()).booleanValue() && !PlayerUtil.mc.field_71439_g.field_70122_E || ((Boolean)keepSprint.reachOnly.getValue()).booleanValue() && RotationUtil.distanceToEntity(target) <= 3.0)) {
                            PlayerUtil.mc.field_71439_g.field_70159_w *= 0.6 + 0.4 * (1.0 - ((Integer)keepSprint.slowdown.getValue()).doubleValue() / 100.0);
                            PlayerUtil.mc.field_71439_g.field_70179_y *= 0.6 + 0.4 * (1.0 - ((Integer)keepSprint.slowdown.getValue()).doubleValue() / 100.0);
                        } else {
                            PlayerUtil.mc.field_71439_g.field_70159_w *= 0.6;
                            PlayerUtil.mc.field_71439_g.field_70179_y *= 0.6;
                            PlayerUtil.mc.field_71439_g.func_70031_b(false);
                        }
                    }
                    if (target instanceof EntityPlayerMP && target.field_70133_I) {
                        ((EntityPlayerMP)target).field_71135_a.func_147359_a((Packet)new S12PacketEntityVelocity(target));
                        target.field_70133_I = false;
                        target.field_70159_w = originalMotionX;
                        target.field_70181_x = originalMotionY;
                        target.field_70179_y = originalMotionZ;
                    }
                    if (isCritical) {
                        PlayerUtil.mc.field_71439_g.func_71009_b(target);
                    }
                    if (enchantmentBonus > 0.0f) {
                        PlayerUtil.mc.field_71439_g.func_71047_c(target);
                    }
                    if (baseDamage >= 18.0f) {
                        PlayerUtil.mc.field_71439_g.func_71029_a((StatBase)AchievementList.field_75999_E);
                    }
                    PlayerUtil.mc.field_71439_g.func_130011_c(target);
                    if (target instanceof EntityLivingBase) {
                        EnchantmentHelper.func_151384_a((EntityLivingBase)((EntityLivingBase)target), (Entity)PlayerUtil.mc.field_71439_g);
                    }
                    EnchantmentHelper.func_151385_b((EntityLivingBase)PlayerUtil.mc.field_71439_g, (Entity)target);
                    if (target instanceof EntityLivingBase) {
                        PlayerUtil.mc.field_71439_g.func_71064_a(StatList.field_75951_w, Math.round(baseDamage * 10.0f));
                        if (fireAspectLevel > 0) {
                            target.func_70015_d(fireAspectLevel * 4);
                        }
                    }
                    PlayerUtil.mc.field_71439_g.func_71020_j(0.3f);
                } else if (isFireAspectApplied) {
                    target.func_70066_B();
                }
            }
        }
    }
}
