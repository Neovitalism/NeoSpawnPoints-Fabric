package me.neovitalism.neospawnpoints;

import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandRegistryInfo;
import me.neovitalism.neospawnpoints.commands.DeleteSpawnCommand;
import me.neovitalism.neospawnpoints.commands.NSPReloadCommand;
import me.neovitalism.neospawnpoints.commands.SetSpawnCommand;
import me.neovitalism.neospawnpoints.commands.SpawnCommand;
import me.neovitalism.neospawnpoints.config.NSPConfig;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;

public class NeoSpawnPoints extends NeoMod {
    private static NeoSpawnPoints instance;

    @Override
    public String getModID() {
        return "NeoSpawnPoints";
    }

    @Override
    public String getModPrefix() {
        return "&#696969[&#9632faN&#9831efe&#9a30e4o&#9b2ed9S&#9d2dcep&#9f2cc3a&#a12bb8w&#a229adn&#a428a2P&#a62797o&#a8268ci&#a92481n&#ab2376t&#ad226bs&#696969]&f ";
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        NeoSpawnPoints.instance = this;
        SpawnManager.initListener();
        this.getLogger().info("Loaded!");
    }

    @Override
    public void configManager() {
        NSPConfig.reload(this.getConfig("config.yml", true));
        SpawnManager.loadSpawns(this.getConfig("spawnpoints.yml", true));
    }

    @Override
    public void registerCommands(CommandRegistryInfo info) {
        new NSPReloadCommand(info.getDispatcher());
        new SpawnCommand(info.getDispatcher());
        new SetSpawnCommand(info.getDispatcher());
        new DeleteSpawnCommand(info.getDispatcher());
    }

    public static NeoSpawnPoints inst() {
        return NeoSpawnPoints.instance;
    }
}
