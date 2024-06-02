package me.neovitalism.neospawnpoints.mixins;

import me.neovitalism.neospawnpoints.NeoSpawnPoints;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "loadPlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/nbt/NbtCompound;",
            at = @At("RETURN"), cancellable = true)
    public void loadPlayerData(ServerPlayerEntity player, @NotNull CallbackInfoReturnable<NbtCompound> cir) {
        NbtCompound playerNBT = cir.getReturnValue();
        SpawnPoint firstJoinSpawn = NeoSpawnPoints.getFirstJoinSpawn();
        boolean joinedBefore = me.neovitalism.neoapi.player.PlayerManager.containsTag(player, "neoapi.joinedBefore");
        if(firstJoinSpawn != null && !joinedBefore) {
            playerNBT = new NbtCompound();
            firstJoinSpawn.includeSpawnNBT(playerNBT);
            player.readNbt(playerNBT);
            cir.setReturnValue(playerNBT);
        } else if(NeoSpawnPoints.shouldForceSpawnOnJoin()) {
            SpawnPoint playerSpawn = SpawnManager.determineSpawnPoint(player);
            if(playerSpawn != null) {
                playerSpawn.includeSpawnNBT(playerNBT);
                player.readNbt(playerNBT);
                cir.setReturnValue(playerNBT);
            }
        }
    }
}
