package me.neovitalism.neospawnpoints.mixins;

import me.neovitalism.neoapi.player.PlayerManager;
import me.neovitalism.neospawnpoints.config.NSPConfig;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnManager;
import me.neovitalism.neospawnpoints.spawnpoints.SpawnPoint;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(net.minecraft.server.PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(
            method = "loadPlayerData",
            at = @At("RETURN")
            , cancellable = true
    )
    public void neoSpawnPoints$loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<Optional<NbtCompound>> cir) {
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

    @Redirect(
            method = "respawnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"
            )
    )
    public TeleportTarget neoSpawnPoints$onPlayerRespawn(ServerPlayerEntity instance, boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        if (!NSPConfig.shouldSpawnAfterDeath(instance)) return instance.getRespawnTarget(alive, postDimensionTransition);
        SpawnPoint spawn = SpawnManager.determineSpawnPoint(instance);
        if (spawn != null) return new TeleportTarget(spawn.getWorld(), spawn.toVec3d(), Vec3d.ZERO, spawn.getYaw(), spawn.getPitch(), postDimensionTransition);
        return instance.getRespawnTarget(alive, postDimensionTransition);
    }

    @Unique
    private Optional<NbtCompound> addSpawnToNBT(ServerPlayerEntity player, NbtCompound playerNBT, SpawnPoint spawnPoint) {
        spawnPoint.setNBTLocation(playerNBT);
        player.readNbt(playerNBT);
        return Optional.of(playerNBT);
    }
}
