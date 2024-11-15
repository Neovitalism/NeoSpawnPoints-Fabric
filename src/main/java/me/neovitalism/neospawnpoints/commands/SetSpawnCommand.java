package me.neovitalism.neospawnpoints.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.neovitalism.neoapi.modloading.command.CommandBase;
import me.neovitalism.neoapi.permissions.NeoPermission;
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

public class SetSpawnCommand extends CommandBase {
    public SetSpawnCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(dispatcher, "setspawn");
    }

    @Override
    public NeoPermission[] getBasePermissions() {
        return NeoPermission.of("neospawnpoints.setspawn").toArray();
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(argument("spawn-point", StringArgumentType.string())
                .executes(context -> this.execute(context.getSource(),
                        context.getArgument("spawn-point", String.class), 0))
                .then(argument("priority", IntegerArgumentType.integer())
                        .executes(context -> this.execute(context.getSource(),
                                context.getArgument("spawn-point", String.class),
                                context.getArgument("priority", Integer.class)))));
    }

    private int execute(ServerCommandSource source, String spawnName, int priority) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendMessage(ColorUtil.parseColour(NeoSpawnPoints.inst().getModPrefix() +
                    "&cThis command can only be used by a player!"));
            return 1;
        }
        if (SpawnManager.hasSpawn(spawnName)) {
            NSPConfig.getLangManager().sendLang(player, "Spawn-Already-Exists", Map.of("{input}", spawnName));
            return 1;
        }
        Map<String, String> replacements = new HashMap<>();
        SpawnPoint newSpawn = SpawnManager.createSpawn(player, spawnName, priority);
        newSpawn.addReplacements(replacements);
        NSPConfig.getLangManager().sendLang(player, "Spawn-Created", replacements);
        return 1;
    }
}
