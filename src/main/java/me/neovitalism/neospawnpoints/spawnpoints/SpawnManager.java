package me.neovitalism.neospawnpoints.spawnpoints;

import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.config.Configuration;
import me.neovitalism.neoapi.world.WorldManager;
import me.neovitalism.neospawnpoints.NeoSpawnPoints;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SpawnManager {
    private static File spawnConfigFile;

    private static final HashMap<String, SpawnPoint> spawnPoints = new HashMap<>();

    public static boolean hasSpawn(String spawnName) {
        return spawnPoints.containsKey(spawnName);
    }

    public static SpawnPoint getSpawn(String spawnName) {
        return spawnPoints.get(spawnName);
    }

    public static List<String> getAllSpawnNames() {
        return new ArrayList<>(spawnPoints.keySet());
    }

    public static void loadSpawns(Configuration config) {
        spawnPoints.clear();
        for(String key : config.getKeys()) {
            Configuration spawnConfig = config.getSection(key);
            if(spawnConfig != null) {
                spawnPoints.put(key,
                        new SpawnPoint(
                                key,
                                WorldManager.getWorldByUUID(UUID.fromString(spawnConfig.getString("world"))),
                                spawnConfig.getDouble("x"),
                                spawnConfig.getDouble("y"),
                                spawnConfig.getDouble("z"),
                                spawnConfig.getFloat("pitch"),
                                spawnConfig.getFloat("yaw"),
                                spawnConfig.getInt("priority")
                        ));
            }
        }
    }

    public static SpawnPoint createSpawn(NeoMod instance, ServerPlayerEntity player, String spawnName, int priority) {
        SpawnPoint newSpawn = new SpawnPoint(
                spawnName,
                player.getServerWorld(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getPitch(),
                player.getYaw(),
                priority);
        spawnPoints.put(spawnName, newSpawn);
        saveSpawns(instance);
        return newSpawn;
    }

    public static void setSpawnConfigFile(File spawnConfigFile) {
        SpawnManager.spawnConfigFile = spawnConfigFile;
    }

    private static void saveSpawns(NeoMod instance) {
        if(spawnPoints.keySet().size() == 0) {
            try {
                FileOutputStream outputStream = new FileOutputStream(spawnConfigFile);
                InputStream in = NeoMod.class.getClassLoader().getResourceAsStream("neospawnpoints/spawnpoints.yml");
                in.transferTo(outputStream);
            } catch (IOException e) {
                NeoSpawnPoints.LOGGER.error("Something went wrong saving spawns!");
            }
        } else {
            Configuration spawnConfig = new Configuration();
            Configuration spawnPointConfig = new Configuration();
            for(String spawnName : spawnPoints.keySet()) {
                spawnPointConfig.set(spawnName, spawnPoints.get(spawnName).toConfiguration());
            }
            spawnConfig.set("SpawnPoints", spawnPointConfig);
            instance.saveConfig(spawnConfigFile, spawnConfig);
        }
    }

    public static SpawnPoint determineSpawnPoint(ServerPlayerEntity player) {
        List<SpawnPoint> possibleTeleports = new ArrayList<>();
        int highestPriority = 0;
        for(String spawnName : spawnPoints.keySet()) {
            if(NeoMod.checkForPermission(player, "neospawnpoints.spawn." + spawnName, 1)) {
                SpawnPoint spawn = spawnPoints.get(spawnName);
                if(spawn.getPriority() > highestPriority) {
                    highestPriority = spawn.getPriority();
                    possibleTeleports.clear();
                    possibleTeleports.add(spawn);
                } else if(spawn.getPriority() == highestPriority) {
                    possibleTeleports.add(spawn);
                }
            }
        }
        if(possibleTeleports.size() == 0) {
            return null;
        } else if(possibleTeleports.size() == 1) {
            return possibleTeleports.get(0);
        } else {
            return randomLocation(possibleTeleports);
        }
    }

    private static SpawnPoint randomLocation(List<SpawnPoint> spawns) {
        double random = (Math.random() * spawns.size());
        return spawns.get((int) random);
    }

    public static void deleteSpawn(NeoMod instance, String spawnName) {
        spawnPoints.remove(spawnName);
        saveSpawns(instance);
    }
}
