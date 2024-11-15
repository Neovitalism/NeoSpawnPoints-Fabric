package me.neovitalism.neospawnpoints.spawnpoints;

import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.helpers.RandomHelper;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neospawnpoints.NeoSpawnPoints;
import me.neovitalism.neospawnpoints.config.NSPConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnManager {
    private static final HashMap<String, SpawnPoint> SPAWN_POINTS = new HashMap<>();

    public static boolean hasSpawn(String spawnName) {
        return SPAWN_POINTS.containsKey(spawnName);
    }

    public static SpawnPoint getSpawn(String spawnName) {
        return SPAWN_POINTS.get(spawnName);
    }

    public static List<String> getAllSpawnNames() {
        return new ArrayList<>(SPAWN_POINTS.keySet());
    }

    public static SpawnPoint determineSpawnPoint(ServerPlayerEntity player) {
        List<SpawnPoint> possibleTeleports = new ArrayList<>();
        int highestPriority = 0;
        for (Map.Entry<String, SpawnPoint> entry : SpawnManager.SPAWN_POINTS.entrySet()) {
            if (!NeoPermission.of("neospawnpoints.spawn." + entry.getKey(), 1).matches(player)) continue;
            SpawnPoint spawn = entry.getValue();
            if (spawn.getPriority() > highestPriority) {
                highestPriority = spawn.getPriority();
                possibleTeleports.clear();
                possibleTeleports.add(spawn);
            } else if (spawn.getPriority() == highestPriority) possibleTeleports.add(spawn);
        }
        if (possibleTeleports.isEmpty()) return null;
        return RandomHelper.getRandomValue(possibleTeleports);
    }

    public static void initListener() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!NSPConfig.shouldSpawnAfterDeath(newPlayer)) return;
            SpawnPoint spawn = SpawnManager.determineSpawnPoint(newPlayer);
            if (spawn != null) spawn.teleport(newPlayer);
        });
    }

    public static SpawnPoint createSpawn(ServerPlayerEntity player, String spawnName, int priority) {
        SpawnPoint newSpawn = new SpawnPoint(player, spawnName, priority);
        SpawnManager.SPAWN_POINTS.put(spawnName, newSpawn);
        SpawnManager.saveSpawns();
        return newSpawn;
    }

    public static void deleteSpawn(String spawnName) {
        SpawnManager.SPAWN_POINTS.remove(spawnName);
        SpawnManager.saveSpawns();
    }

    public static void loadSpawns(Configuration config) {
        SpawnManager.SPAWN_POINTS.clear();
        for (String key : config.getKeys()) {
            Configuration spawnConfig = config.getSection(key);
            if (spawnConfig == null) continue;
            SpawnManager.SPAWN_POINTS.put(key, new SpawnPoint(key, spawnConfig));
        }
    }

    private static void saveSpawns() {
        if (SpawnManager.SPAWN_POINTS.isEmpty()) {
            NeoSpawnPoints.inst().saveResource("spawnpoints.yml", true);
            return;
        }
        Configuration spawnConfig = new Configuration();
        Configuration spawnPointConfig = new Configuration();
        SpawnManager.SPAWN_POINTS.forEach((name, spawn) -> spawnPointConfig.set(name, spawn.toConfiguration()));
        spawnConfig.set("SpawnPoints", spawnPointConfig);
        NeoSpawnPoints.inst().saveConfig("spawnpoints.yml", spawnConfig);
    }
}
