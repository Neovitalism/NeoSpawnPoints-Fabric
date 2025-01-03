package me.neovitalism.neospawnpoints.mixins;

import me.neovitalism.neoapi.player.PlayerManager;
import me.neovitalism.neospawnpoints.config.NSPConfig;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(net.minecraft.server.PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "loadPlayerData", at = @At("RETURN"), cancellable = true)
    public void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
        SpawnPoint firstJoinSpawn = NSPConfig.getFirstJoinSpawn();
        boolean joinedBefore = PlayerManager.containsTag(player, "neoapi.joinedBefore");
        if (firstJoinSpawn != null && !joinedBefore) {
            cir.setReturnValue(this.addSpawnToNBT(player, cir.getReturnValue().orElse(new NbtCompound()), firstJoinSpawn));
        } else if (NSPConfig.shouldForceSpawnOnJoin()) {
            SpawnPoint playerSpawn = SpawnManager.determineSpawnPoint(player);
            if (playerSpawn != null) cir.setReturnValue(this.addSpawnToNBT(player,
                    cir.getReturnValue().orElse(new NbtCompound()), playerSpawn));
        }
    }

    @Unique
    private Optional<NbtCompound> addSpawnToNBT(ServerPlayerEntity player, NbtCompound playerNBT, SpawnPoint spawnPoint) {
        spawnPoint.setNBTLocation(playerNBT);
        player.readNbt(playerNBT);
        return Optional.of(playerNBT);
    }
}
