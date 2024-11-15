package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.modloading.command.SuggestionProviders;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neoapi.player.PlayerManager;
import me.neovitalism.neoapi.utils.ColorUtil;
import me.neovitalism.neospawnpoints.NeoSpawnPoints;
import me.neovitalism.neospawnpoints.config.NSPConfig;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;

public class SpawnCommand extends CommandBase {
    public SpawnCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(dispatcher, "spawn");
    }

    @Override
    public NeoPermission[] getBasePermissions() {
        return NeoPermission.of("neospawnpoints.spawn", 0).toArray();
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player != null) {
                SpawnPoint playerSpawn = SpawnManager.determineSpawnPoint(player);
                if (playerSpawn == null) {
                    NSPConfig.getLangManager().sendLang(player, "No-Spawn-Message", null);
                } else {
                    Map<String, String> replacements = new HashMap<>();
                    playerSpawn.addReplacements(replacements);
                    playerSpawn.teleport(player);
                    NSPConfig.getLangManager().sendLang(player, "Teleported-Message", replacements);
                }
            } else context.getSource().sendMessage(ColorUtil.parseColour(NeoSpawnPoints.inst().getModPrefix() +
                    "&cThis command can only be used by a player! Try adding arguments."));
            return Command.SINGLE_SUCCESS;
        }).then(argument("player", StringArgumentType.string())
                .requires(NeoPermission.of("neospawnpoints.spawn.other", 2)::matches)
                .suggests(((context, builder) -> new SuggestionProviders.Player(
                        NeoPermission.of("neospawnpoints.spawn.all").matches(context.getSource())
                ).getSuggestions(context, builder)))
                .executes(context -> {
                    String playerName = context.getArgument("player", String.class);
                    Map<String, String> replacements = new HashMap<>();
                    if (playerName.equals("all")) {
                        if (NeoPermission.of("neospawnpoints.all").matches(context.getSource())) {
                            NSPConfig.getLangManager().sendLang(context.getSource(),
                                    "Invalid-Permission-Spawn-All", null);
                            return Command.SINGLE_SUCCESS;
                        }
                        int playerCount = 0;
                        int successfullyTeleported = 0;
                        for (ServerPlayerEntity target : PlayerManager.getOnlinePlayers()) {
                            playerCount++;
                            SpawnPoint spawnPoint = SpawnManager.determineSpawnPoint(target);
                            if (spawnPoint == null) continue;
                            spawnPoint.addReplacements(replacements);
                            spawnPoint.teleport(target);
                            NSPConfig.getLangManager().sendLang(target, "Force-Spawn-Message-Player", replacements);
                            successfullyTeleported++;
                            replacements.clear();
                        }
                        replacements.put("{amount}", String.valueOf(playerCount));
                        replacements.put("{succeeded}", String.valueOf(successfullyTeleported));
                        NSPConfig.getLangManager().sendLang(context.getSource(), "Teleported-All-Spawn-Admin", replacements);
                        return Command.SINGLE_SUCCESS;
                    }
                    ServerPlayerEntity target = PlayerManager.getPlayer(playerName);
                    if (target == null) {
                        replacements.put("{input}", playerName);
                        NSPConfig.getLangManager().sendLang(context.getSource(), "Invalid-Player", replacements);
                        return Command.SINGLE_SUCCESS;
                    }
                    SpawnPoint spawnPoint = SpawnManager.determineSpawnPoint(target);
                    if (spawnPoint == null) {
                        NSPConfig.getLangManager().sendLang(context.getSource(), "No-Spawn-Message-Admin", replacements);
                        return Command.SINGLE_SUCCESS;
                    }
                    spawnPoint.addReplacements(replacements);
                    spawnPoint.teleport(target);
                    NSPConfig.getLangManager().sendLang(target, "Force-Spawn-Message-Player", replacements);
                    replacements.put("{player}", target.getName().getString());
                    NSPConfig.getLangManager().sendLang(context.getSource(), "Force-Spawn-Message-Admin", replacements);
                    return Command.SINGLE_SUCCESS;
                }).then(argument("spawn-point", StringArgumentType.string())
                        .suggests((context, builder) -> new SuggestionProviders.List("spawn-point",
                                SpawnManager.getAllSpawnNames()).getSuggestions(context, builder))
                        .executes(context -> {
                            String playerName = context.getArgument("player", String.class);
                            Map<String, String> replacements = new HashMap<>();
                            if (playerName.equals("all")) {
                                if (!NeoPermission.of("neospawnpoints.all").matches(context.getSource())) {
                                    NSPConfig.getLangManager().sendLang(context.getSource(),
                                            "Invalid-Permission-Spawn-All", null);
                                    return Command.SINGLE_SUCCESS;
                                }
                                String spawnName = context.getArgument("spawn-point", String.class);
                                if (!SpawnManager.hasSpawn(spawnName)) {
                                    replacements.put("{input}", spawnName);
                                    NSPConfig.getLangManager().sendLang(context.getSource(),
                                            "Invalid-Spawn-Point", replacements);
                                    return Command.SINGLE_SUCCESS;
                                }
                                SpawnPoint spawnPoint = SpawnManager.getSpawn(spawnName);
                                spawnPoint.addReplacements(replacements);
                                int playerCount = 0;
                                for (ServerPlayerEntity target : PlayerManager.getOnlinePlayers()) {
                                    playerCount++;
                                    spawnPoint.teleport(target);
                                    NSPConfig.getLangManager().sendLang(target,"Force-Specific-Spawn-Player", replacements);
                                }
                                replacements.put("{amount}", String.valueOf(playerCount));
                                NSPConfig.getLangManager().sendLang(context.getSource(), "Teleported-All-Specific-Spawn-Admin", replacements);
                                return Command.SINGLE_SUCCESS;
                            }
                            ServerPlayerEntity target = PlayerManager.getPlayer(playerName);
                            if (target == null) {
                                replacements.put("{input}", playerName);
                                NSPConfig.getLangManager().sendLang(context.getSource(), "Invalid-Player", replacements);
                                return Command.SINGLE_SUCCESS;
                            }
                            String spawnName = context.getArgument("spawn-point", String.class);
                            if (!SpawnManager.hasSpawn(spawnName)) {
                                replacements.put("{input}", spawnName);
                                NSPConfig.getLangManager().sendLang(context.getSource(),
                                        "Invalid-Spawn-Point", replacements);
                                return Command.SINGLE_SUCCESS;
                            }
                            SpawnPoint spawnPoint = SpawnManager.getSpawn(spawnName);
                            spawnPoint.addReplacements(replacements);
                            spawnPoint.teleport(target);
                            NSPConfig.getLangManager().sendLang(target,"Force-Specific-Spawn-Player", replacements);
                            replacements.put("{player}", target.getName().getString());
                            NSPConfig.getLangManager().sendLang(context.getSource(),
                                    "Force-Specific-Spawn-Admin", replacements);
                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
