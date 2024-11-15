package me.neovitalism.neospawnpoints.config;

import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.lang.LangManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.server.network.ServerPlayerEntity;

public class NSPConfig {
    private static SpawnPoint firstJoinSpawn = null;
    private static boolean forceSpawnOnJoin = false;
    private static boolean spawnNoRespawn = true;
    private static boolean forceSpawnOnDeath = false;

    private static LangManager langManager = null;

    public static void reload(Configuration config) {
        String firstJoinSpawnName = config.getString("First-Join-Spawn", "");
        NSPConfig.firstJoinSpawn = SpawnManager.getSpawn(firstJoinSpawnName);
        NSPConfig.forceSpawnOnJoin = config.getBoolean("Force-Spawn-On-Join", false);
        NSPConfig.spawnNoRespawn = config.getBoolean("Spawn-No-Respawn", true);
        NSPConfig.forceSpawnOnDeath = config.getBoolean("Force-Spawn-On-Death", false);
        NSPConfig.langManager = new LangManager(config.getSection("Lang"));
    }

    public static SpawnPoint getFirstJoinSpawn() {
        return NSPConfig.firstJoinSpawn;
    }

    public static boolean shouldForceSpawnOnJoin() {
        return NSPConfig.forceSpawnOnJoin;
    }

    public static boolean shouldSpawnAfterDeath(ServerPlayerEntity player) {
        return NSPConfig.forceSpawnOnDeath || (NSPConfig.spawnNoRespawn && (player.getSpawnPointPosition() == null));
    }

    public static LangManager getLangManager() {
        return NSPConfig.langManager;
    }
}
