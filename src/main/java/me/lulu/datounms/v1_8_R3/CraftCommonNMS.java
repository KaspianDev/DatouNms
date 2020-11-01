package me.lulu.datounms.v1_8_R3;

import me.lulu.datounms.CommonNMS;
import me.lulu.datounms.model.ArmorInfo;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.mineacademy.fo.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CraftCommonNMS extends CommonNMS {

    private static final HashMap<UUID, Integer> sitPlayers = new HashMap<>();

    @Override
    public float getAbsorptionHeart(Player p) {
        return ((CraftPlayer) p).getHandle().getAbsorptionHearts();
    }

    @Override
    public String getMinecraftSoundKey(SentPacket packet) {
        return packet.getPacketValue(0).toString();
    }

    @Override
    protected String getBreakSoundString(Material material) {
        MinecraftKey materialKey = new MinecraftKey(material.name().toLowerCase());
        net.minecraft.server.v1_8_R3.Block nmsBlock = net.minecraft.server.v1_8_R3.Block.REGISTRY.get(materialKey).getBlockData().getBlock();

        return nmsBlock.stepSound.getBreakSound();
    }

    @Override
    public double getArmorPoint(ArmorInfo armorInfo) {
        return ItemArmor.EnumArmorMaterial
                .valueOf(armorInfo.getMaterial())
                .b(armorInfo.getEquipmentSlotNumber());
    }

    @Override
    public void playDeathAnimation(Player player) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld, cp.getProfile(), new PlayerInteractManager(nmsWorld));
        npc.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle());
        PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc);
        PacketPlayOutNamedEntitySpawn entitySpawn = new PacketPlayOutNamedEntitySpawn(npc);
        PacketPlayOutEntityStatus entityDeath = new PacketPlayOutEntityStatus(npc, (byte) 3);
        List<Player> toPlayRemove = new ArrayList<>();
        for (Entity o : player.getNearbyEntities(16, 16, 16)) {
            if (o instanceof Player) {
                PlayerConnection connection = ((CraftPlayer) o).getHandle().playerConnection;
                connection.sendPacket(removePlayer);
                connection.sendPacket(addPlayer);
                connection.sendPacket(entitySpawn);
                connection.sendPacket(entityDeath);
                toPlayRemove.add((Player) o);
            }
        }
        Common.runLater(1, () -> {
            for (Player o : toPlayRemove) {
                if (o.isOnline()) {
                    PlayerConnection connection = ((CraftPlayer) o).getHandle().playerConnection;
                    connection.sendPacket(removePlayer);
                }
            }
        });
    }

    @Override
    public void setCanPickupExp(Player p, boolean b) {
        CraftPlayer craftPlayer = (CraftPlayer) p;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        entityPlayer.bp = b ? 0 : Integer.MAX_VALUE;
    }
}