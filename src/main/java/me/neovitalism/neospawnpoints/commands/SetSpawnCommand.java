package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.utils.ChatUtil;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SetSpawnCommand implements CommandBase {
    public SetSpawnCommand(NeoMod instance, CommandDispatcher<ServerCommandSource> dispatcher) {
        register(instance, dispatcher);
    }

    @Override
    public String[] getCommandAliases() {
        return new String[0];
    }

    @Override
    public LiteralCommandNode<ServerCommandSource> register(NeoMod instance, CommandDispatcher<ServerCommandSource> dispatcher) {
        Map<String, String> replacements = new HashMap<>();
        return dispatcher.register(literal("setspawn")
                .requires(serverCommandSource ->
                        NeoMod.checkForPermission(serverCommandSource, "neospawnpoints.setspawn", 4))
                .then(argument("spawn-point", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if(player != null) {
                                String spawnName = context.getArgument("spawn-point", String.class);
                                if (SpawnManager.hasSpawn(spawnName)) {
                                    replacements.put("{input}", spawnName);
                                    ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Spawn-Already-Exists", replacements);
                                } else createSpawn(instance, player, spawnName, 0);
                            } else {
                                ChatUtil.sendPrettyMessage(context.getSource(), instance.getModPrefix(), "&cThis command can only be used by a player!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("priority", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if(player != null) {
                                        String spawnName = context.getArgument("spawn-point", String.class);
                                        if (SpawnManager.hasSpawn(spawnName)) {
                                            replacements.put("{input}", spawnName);
                                            ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Spawn-Already-Exists", replacements);
                                        } else createSpawn(instance, player, spawnName, context.getArgument("priority", Integer.class));
                                    } else {
                                        ChatUtil.sendPrettyMessage(context.getSource(), instance.getModPrefix(), "&cThis command can only be used by a player!");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private void createSpawn(NeoMod instance, ServerPlayerEntity player, String spawnName, int priority) {
        Map<String, String> replacements = new HashMap<>();
        SpawnPoint newSpawn = SpawnManager.createSpawn(instance, player, spawnName, priority);
        newSpawn.addReplacements(replacements);
        ChatUtil.sendPrettyMessage(instance, player, false, "Spawn-Created", replacements);
    }
}
