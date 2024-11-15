package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.modloading.command.SuggestionProviders;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neospawnpoints.config.NSPConfig;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;

public class DeleteSpawnCommand extends CommandBase {
    public DeleteSpawnCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(dispatcher, "delspawn");
    }

    @Override
    public NeoPermission[] getBasePermissions() {
        return NeoPermission.of("neospawnpoints.delspawn").toArray();
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("spawn-point", StringArgumentType.string())
                .suggests((context, builder) -> new SuggestionProviders.List("spawn-point",
                        SpawnManager.getAllSpawnNames()).getSuggestions(context, builder))
                .executes(context -> {
                    String spawnName = context.getArgument("spawn-point", String.class);
                    if (!SpawnManager.hasSpawn(spawnName)) {
                        NSPConfig.getLangManager().sendLang(context.getSource(),
                                "Invalid-Spawn-Point", Map.of("{input}", spawnName));
                        return Command.SINGLE_SUCCESS;
                    }
                    SpawnManager.deleteSpawn(spawnName);
                    NSPConfig.getLangManager().sendLang(context.getSource(),
                            "Spawn-Deleted", Map.of("{spawn}", spawnName));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
