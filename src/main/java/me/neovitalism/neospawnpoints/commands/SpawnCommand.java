package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.modloading.command.ListSuggestionProvider;
import me.neovitalism.neoapi.modloading.command.PlayerSuggestionProvider;
import me.neovitalism.neoapi.utils.ChatUtil;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpawnCommand implements CommandBase {
    public SpawnCommand(NeoMod instance, CommandDispatcher<ServerCommandSource> dispatcher) {
        register(instance, dispatcher);
    }

    @Override
    public String[] getCommandAliases() {
        return new String[0];
    }

    @Override
    public LiteralCommandNode<ServerCommandSource> register(NeoMod instance, CommandDispatcher<ServerCommandSource> dispatcher) {
        Map<String, String> replacements = new HashMap<>();
        return dispatcher.register(literal("spawn")
                .requires(serverCommandSource ->
                        NeoMod.checkForPermission(serverCommandSource, "neospawnpoints.spawn", 0))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if(player != null) {
                        SpawnPoint playerSpawn = SpawnManager.determineSpawnPoint(player);
                        if(playerSpawn == null) {
                            ChatUtil.sendPrettyMessage(instance, player, false, "No-Spawn-Message");
                        } else {
                            playerSpawn.addReplacements(replacements);
                            playerSpawn.teleport(player);
                            ChatUtil.sendPrettyMessage(instance, player, false, "Teleported-Message", replacements);
                        }
                    } else {
                        ChatUtil.sendPrettyMessage(context.getSource(), instance.getModPrefix(), "&cThis command can only be used by a player!");
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(argument("player", StringArgumentType.string())
                        .requires(serverCommandSource ->
                                NeoMod.checkForPermission(serverCommandSource, "neospawnpoints.spawn.other", 3)
                        )
                        .suggests(((context, builder) -> new PlayerSuggestionProvider(instance, NeoMod.checkForPermission(context.getSource(), "neospawnpoints.spawn.all", 4)).getSuggestions(context, builder)))
                        .executes(context -> {
                            String playerName = context.getArgument("player", String.class);
                            if(!playerName.equals("all")) {
                                ServerPlayerEntity target = instance.getServer().getPlayerManager().getPlayer(playerName);
                                if (target != null) {
                                    SpawnPoint spawnPoint = SpawnManager.determineSpawnPoint(target);
                                    if (spawnPoint != null) {
                                        spawnPoint.addReplacements(replacements);
                                        spawnPoint.teleport(target);
                                        ChatUtil.sendPrettyMessage(instance, target, false, "Force-Spawn-Message-Player", replacements);
                                        replacements.put("{player}", target.getName().getString());
                                        ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Force-Spawn-Message-Admin", replacements);
                                    } else {
                                        ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "No-Spawn-Message-Admin", replacements);
                                    }
                                } else {
                                    replacements.put("{input}", playerName);
                                    ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Player", replacements);
                                }
                            } else {
                                if(NeoMod.checkForPermission(context.getSource(), "neospawnpoints.spawn.all", 4)) {
                                    int playerCount = 0;
                                    int successfullyTeleported = 0;
                                    List<ServerPlayerEntity> onlinePlayers = instance.getServer().getPlayerManager().getPlayerList();
                                    for(ServerPlayerEntity target : onlinePlayers) {
                                        playerCount++;
                                        SpawnPoint spawnPoint = SpawnManager.determineSpawnPoint(target);
                                        if(spawnPoint != null) {
                                            spawnPoint.addReplacements(replacements);
                                            spawnPoint.teleport(target);
                                            ChatUtil.sendPrettyMessage(instance, target, false, "Force-Spawn-Message-Player", replacements);
                                            successfullyTeleported++;
                                            replacements.clear();
                                        }
                                    }
                                    replacements.put("{amount}", String.valueOf(playerCount));
                                    replacements.put("{succeeded}", String.valueOf(successfullyTeleported));
                                    ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Teleported-All-Spawn-Admin", replacements);
                                } else {
                                    ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Permission-Spawn-All");
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(argument("spawn-point", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    return new ListSuggestionProvider("spawn-point", SpawnManager.getAllSpawnNames()).getSuggestions(context, builder);
                                })
                                .executes(context -> {
                                    String playerName = context.getArgument("player", String.class);
                                    if(!playerName.equals("all")) {
                                        ServerPlayerEntity target = instance.getServer().getPlayerManager().getPlayer(playerName);
                                        if (target != null) {
                                            String spawnName = context.getArgument("spawn-point", String.class);
                                            if (SpawnManager.hasSpawn(spawnName)) {
                                                SpawnPoint spawnPoint = SpawnManager.getSpawn(spawnName);
                                                spawnPoint.addReplacements(replacements);
                                                spawnPoint.teleport(target);
                                                ChatUtil.sendPrettyMessage(instance, target, false, "Force-Specific-Spawn-Player", replacements);
                                                replacements.put("{player}", target.getName().getString());
                                                ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Force-Specific-Spawn-Admin", replacements);
                                            } else {
                                                replacements.put("{input}", spawnName);
                                                ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Spawn-Point", replacements);
                                            }
                                        } else {
                                            replacements.put("{input}", playerName);
                                            ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Player", replacements);
                                        }
                                    } else {
                                        if(NeoMod.checkForPermission(context.getSource(), "neospawnpoints.spawn.all", 4)) {
                                            String spawnName = context.getArgument("spawn-point", String.class);
                                            if (SpawnManager.hasSpawn(spawnName)) {
                                                SpawnPoint spawnPoint = SpawnManager.getSpawn(spawnName);
                                                spawnPoint.addReplacements(replacements);
                                                int playerCount = 0;
                                                List<ServerPlayerEntity> onlinePlayers = instance.getServer().getPlayerManager().getPlayerList();
                                                for(ServerPlayerEntity target : onlinePlayers) {
                                                    playerCount++;
                                                    spawnPoint.teleport(target);
                                                    ChatUtil.sendPrettyMessage(instance, target, false, "Force-Specific-Spawn-Player", replacements);
                                                }
                                                replacements.put("{amount}", String.valueOf(playerCount));
                                                ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Teleported-All-Specific-Spawn-Admin", replacements);
                                            } else {
                                                replacements.put("{input}", spawnName);
                                                ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Spawn-Point", replacements);
                                            }
                                        } else {
                                            ChatUtil.sendPrettyMessage(instance, context.getSource(), false, "Invalid-Permission-Spawn-All");
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
