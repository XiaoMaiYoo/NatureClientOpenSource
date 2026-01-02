package myau.util;

import myau.util.RandomUtil;
import myau.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class BlockUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static boolean isReplaceable(BlockPos blockPos) {
        return BlockUtil.isReplaceable(BlockUtil.mc.field_71441_e.func_180495_p(blockPos).func_177230_c());
    }

    public static boolean isReplaceable(Block block) {
        if (!block.func_149688_o().func_76222_j()) {
            return false;
        }
        if (!(block instanceof BlockSnow)) {
            return true;
        }
        return !(block.func_149669_A() > 0.125);
    }

    public static boolean isInteractable(BlockPos blockPos) {
        return BlockUtil.isInteractable(BlockUtil.mc.field_71441_e.func_180495_p(blockPos).func_177230_c());
    }

    public static boolean isInteractable(Block block) {
        if (block instanceof BlockContainer) {
            return true;
        }
        if (block instanceof BlockWorkbench) {
            return true;
        }
        if (block instanceof BlockAnvil) {
            return true;
        }
        if (block instanceof BlockBed) {
            return true;
        }
        if (block instanceof BlockDoor && block.func_149688_o() != Material.field_151573_f) {
            return true;
        }
        if (block instanceof BlockTrapDoor) {
            return true;
        }
        if (block instanceof BlockFenceGate) {
            return true;
        }
        if (block instanceof BlockFence) {
            return true;
        }
        if (block instanceof BlockButton) {
            return true;
        }
        if (block instanceof BlockLever) {
            return true;
        }
        return block instanceof BlockJukebox;
    }

    public static boolean isSolid(Block block) {
        if (block instanceof BlockStairs) {
            return false;
        }
        if (block instanceof BlockSlab) {
            return false;
        }
        if (block instanceof BlockEndPortalFrame) {
            return false;
        }
        if (block instanceof BlockEndPortal) {
            return false;
        }
        if (block instanceof BlockVine) {
            return false;
        }
        if (block instanceof BlockPumpkin) {
            return false;
        }
        if (block instanceof BlockCactus) {
            return false;
        }
        if (block instanceof BlockBush) {
            return false;
        }
        if (block instanceof BlockFalling) {
            return false;
        }
        if (block instanceof BlockWeb) {
            return false;
        }
        if (block instanceof BlockPane) {
            return false;
        }
        if (block instanceof BlockCarpet) {
            return false;
        }
        if (block instanceof BlockSnow) {
            return false;
        }
        if (block instanceof BlockFence) {
            return false;
        }
        if (block instanceof BlockFenceGate) {
            return false;
        }
        if (block instanceof BlockWall) {
            return false;
        }
        if (block instanceof BlockLadder) {
            return false;
        }
        if (block instanceof BlockTorch) {
            return false;
        }
        if (block instanceof BlockRedstoneWire) {
            return false;
        }
        if (block instanceof BlockRedstoneDiode) {
            return false;
        }
        if (block instanceof BlockBasePressurePlate) {
            return false;
        }
        if (block instanceof BlockTripWire) {
            return false;
        }
        if (block instanceof BlockTripWireHook) {
            return false;
        }
        if (block instanceof BlockRailBase) {
            return false;
        }
        if (block instanceof BlockSlime) {
            return false;
        }
        return !(block instanceof BlockTNT);
    }

    public static Vec3 getHitVec(BlockPos blockPos, EnumFacing enumFacing, float yaw, float pitch) {
        MovingObjectPosition movingObjectPosition = RotationUtil.rayTrace(yaw, pitch, (double)BlockUtil.mc.field_71442_b.func_78757_d(), 1.0f);
        if (movingObjectPosition != null && movingObjectPosition.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK && movingObjectPosition.func_178782_a().equals((Object)blockPos) && movingObjectPosition.field_178784_b == enumFacing) {
            return movingObjectPosition.field_72307_f;
        }
        return BlockUtil.getClickVec(blockPos, enumFacing);
    }

    public static Vec3 getClickVec(BlockPos blockPos, EnumFacing enumFacing) {
        Block block = BlockUtil.mc.field_71441_e.func_180495_p(blockPos).func_177230_c();
        Vec3 vec3 = new Vec3((double)blockPos.func_177958_n() + Math.min(Math.max(RandomUtil.nextDouble(0.0, 1.0), block.func_149704_x()), block.func_149753_y()), (double)blockPos.func_177956_o() + Math.min(Math.max(RandomUtil.nextDouble(0.0, 1.0), block.func_149665_z()), block.func_149669_A()), (double)blockPos.func_177952_p() + Math.min(Math.max(RandomUtil.nextDouble(0.0, 1.0), block.func_149706_B()), block.func_149693_C()));
        switch (enumFacing) {
            default: {
                return new Vec3(vec3.field_72450_a, (double)blockPos.func_177956_o() + block.func_149665_z(), vec3.field_72449_c);
            }
            case UP: {
                return new Vec3(vec3.field_72450_a, (double)blockPos.func_177956_o() + block.func_149669_A(), vec3.field_72449_c);
            }
            case NORTH: {
                return new Vec3(vec3.field_72450_a, vec3.field_72448_b, (double)blockPos.func_177952_p() + block.func_149706_B());
            }
            case EAST: {
                return new Vec3((double)blockPos.func_177958_n() + block.func_149753_y(), vec3.field_72448_b, vec3.field_72449_c);
            }
            case SOUTH: {
                return new Vec3(vec3.field_72450_a, vec3.field_72448_b, (double)blockPos.func_177952_p() + block.func_149693_C());
            }
            case WEST: 
        }
        return new Vec3((double)blockPos.func_177958_n() + block.func_149704_x(), vec3.field_72448_b, vec3.field_72449_c);
    }
}
