package me.neovitalism.neospawnpoints.spawnpoints;

import me.neovitalism.neoapi.modloading.config.Configuration;
import me.neovitalism.neoapi.world.WorldManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public record SpawnPoint(String name, ServerWorld world, double x, double y, double z, float pitch, float yaw, int priority) {
    public Configuration createConfig() {
        Configuration spawnConfig = new Configuration();
        spawnConfig.set("world", String.valueOf(WorldManager.getWorldUUID(world)));
        spawnConfig.set("x", x);
        spawnConfig.set("y", y);
        spawnConfig.set("z", z);
        spawnConfig.set("pitch", pitch);
        spawnConfig.set("yaw", yaw);
        spawnConfig.set("priority", priority);
        return spawnConfig;
    }

    public void addReplacements(Map<String, String> replacements) {
        replacements.put("{spawn}", name);
        replacements.put("{priority}", String.valueOf(priority));
        replacements.put("{x}", String.valueOf(x));
        replacements.put("{y}", String.valueOf(y));
        replacements.put("{z}", String.valueOf(z));
        replacements.put("{pitch}", String.valueOf(pitch));
        replacements.put("{yaw}", String.valueOf(yaw));
        replacements.put("{world}", WorldManager.getWorldName(world));
    }

    public void teleport(ServerPlayerEntity player) {
        player.teleport(world, x, y, z, yaw, pitch);
    }
}
