package me.lulu.datounms.v1_16_R1;

import me.lulu.datounms.WorldNMS;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;

/**
 * 2018-12-19 上午 11:58
 */
public class CraftWorldNMS implements WorldNMS {

    @Override
    public void setBlockSuperFast(org.bukkit.World world, int x, int y, int z, Material material, byte data, boolean applyPhysics) {
        setBlockSuperFast(new Location(world, x, y, z), material, data, applyPhysics);
    }

    @Override
    public void setBlockSuperFast(Location loc, Material material, byte data, boolean applyPhysics) {
        World w = ((CraftWorld) loc.getWorld()).getHandle();
        Chunk chunk = w.getChunkAt(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
        BlockPosition bp = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        IBlockData ibd = IRegistry.BLOCK.get(new MinecraftKey(material.name().toLowerCase())).getBlockData();
        if (applyPhysics) {
            w.setTypeAndData(bp, ibd, 3);
        } else {
            w.setTypeAndData(bp, ibd, 2);
        }
    }

    @Override
    public void spawnOrb(Location location, int amount, int value) {
        double x = location.getX(), y = location.getY(), z = location.getZ();
        World w = ((CraftWorld) location.getWorld()).getHandle();
        for (int i = 0; i < amount; i++) {
            w.addEntity(new EntityExperienceOrb(w, x, y, z, (int) Math.pow(value, 0.5)));
        }
    }

    @Override
    public int getMinecraftRegistryBlockID(Material material) {
        MinecraftKey materialKey = new MinecraftKey(material.name().toLowerCase());
        return Block.getCombinedId(IRegistry.BLOCK.get(materialKey).getBlockData());
    }
}