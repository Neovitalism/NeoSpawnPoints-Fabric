package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.modloading.command.ListSuggestionProvider;
import me.neovitalism.neoapi.utils.ChatUtil;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteSpawnCommand implements CommandBase {
    public DeleteSpawnCommand(NeoMod instance, CommandDispatcher<ServerCommandSource> dispatcher) {
        register(instance, dispatcher);
    }

    @Override
    public String[] getCommandAliases() {
        return new String[0];
    }

    @Override
    public LiteralCommandNode<ServerCommandSource> register(NeoMod instance, CommandDispatcher<ServerCommandSource> dispatcher) {
        Map<String, String> replacements = new HashMap<>();
        return dispatcher.register(literal("delspawn")
                .requires(serverCommandSource ->
                        NeoMod.checkForPermission(serverCommandSource, "neospawnpoints.delspawn", 4))
                .then(argument("spawn-point", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            return new ListSuggestionProvider("spawn-point", SpawnManager.getAllSpawnNames()).getSuggestions(context, builder);
                        })
                        .executes(context -> {
                            String spawnName = context.getArgument("spawn-point", String.class);
                            if (SpawnManager.hasSpawn(spawnName)) {
                                SpawnManager.deleteSpawn(instance, spawnName);
                                replacements.put("{spawn}", spawnName);
                                ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Spawn-Deleted", replacements);
                            } else {
                                replacements.put("{input}", spawnName);
                                ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Spawn-Point", replacements);
                            }
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
