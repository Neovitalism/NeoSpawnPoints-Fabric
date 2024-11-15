package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.neovitalism.neoapi.modloading.command.ReloadCommand;
import me.neovitalism.neospawnpoints.NeoSpawnPoints;
import net.minecraft.server.command.ServerCommandSource;

public final class NSPReloadCommand extends ReloadCommand {
    public NSPReloadCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(NeoSpawnPoints.inst(), dispatcher, "neospawnpoints", "nsp");
    }
}
