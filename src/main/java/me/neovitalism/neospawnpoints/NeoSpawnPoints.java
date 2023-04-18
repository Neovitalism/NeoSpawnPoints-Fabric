package me.neovitalism.neospawnpoints;

import me.neovitalism.neoapi.events.JoinEvent;
import me.neovitalism.neoapi.lang.LangManager;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.config.Configuration;
import me.neovitalism.neospawnpoints.commands.DeleteSpawnCommand;
import me.neovitalism.neospawnpoints.commands.NSPReloadCommand;
import me.neovitalism.neospawnpoints.commands.SetSpawnCommand;
import me.neovitalism.neospawnpoints.commands.SpawnCommand;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NeoSpawnPoints extends NeoMod {
    public static final Logger LOGGER = LoggerFactory.getLogger("NeoSpawnPoints");
    private LangManager langManager;

    @Override
    public String getModID() {
        return "NeoSpawnPoints";
    }

    @Override
    public String getModPrefix() {
        return "&#696969[&#9632faN&#9831efe&#9a30e4o&#9b2ed9S&#9d2dcep&#9f2cc3a&#a12bb8w&#a229adn&#a428a2P&#a62797o&#a8268ci&#a92481n&#ab2376t&#ad226bs&#696969]&f ";
    }

    @Override
    public LangManager getLangManager() {
        return langManager;
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            new NSPReloadCommand(this, dispatcher);
            new SpawnCommand(this, dispatcher);
            new SetSpawnCommand(this, dispatcher);
            new DeleteSpawnCommand(this, dispatcher);
        });
        JoinEvent.EVENT.register((player, hasJoinedBefore) -> {
            if(firstJoinSpawn != null && !hasJoinedBefore) {
                firstJoinSpawn.teleport(player);
            } else if(forceSpawnOnJoin) {
                SpawnPoint playerSpawn = SpawnManager.determineSpawnPoint(player);
                if(playerSpawn != null) {
                    playerSpawn.teleport(player);
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if(forceSpawnOnDeath || (spawnNoRespawn && (newPlayer.getSpawnPointPosition() == null))) {
                SpawnPoint spawn = SpawnManager.determineSpawnPoint(newPlayer);
                if(spawn != null) {
                    spawn.teleport(newPlayer);
                }
            }
        });
        LOGGER.info("Loaded!");
    }

    @Override
    public void configManager() {
        Configuration config = getDefaultConfig();
        this.langManager = new LangManager(config.getSection("Lang"));
        try {
            SpawnManager.setSpawnConfigFile(getOrCreateConfigurationFile("spawnpoints.yml"));
        } catch (IOException e) {
            LOGGER.error("Something went wrong fetching the spawnpoints.yml file!");
        }
        Configuration spawnConfig = getConfig("spawnpoints.yml");
        if(spawnConfig.contains("SpawnPoints")) {
            SpawnManager.loadSpawns(spawnConfig.getSection("SpawnPoints"));
        }
        String firstJoinSpawnName = config.getString("First-Join-Spawn", "");
        firstJoinSpawn = SpawnManager.getSpawn(firstJoinSpawnName);
        forceSpawnOnJoin = config.getBoolean("Force-Spawn-On-Join", false);
        spawnNoRespawn = config.getBoolean("Spawn-No-Respawn", true);
        forceSpawnOnDeath = config.getBoolean("Force-Spawn-On-Death", false);
    }

    private SpawnPoint firstJoinSpawn = null;
    private boolean forceSpawnOnJoin = false;
    private boolean spawnNoRespawn = true;
    private boolean forceSpawnOnDeath = false;
}
