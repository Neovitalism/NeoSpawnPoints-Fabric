package me.neovitalism.neospawnpoints.spawnpoints;

import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class SpawnPoint extends Location {
    private final String name;
    private final int priority;

    public SpawnPoint(ServerPlayerEntity player, String spawnName, int priority) {
        super(player);
        this.name = spawnName;
        this.priority = priority;
    }

    public SpawnPoint(String name, Configuration config) {
        super(config);
        this.name = name;
        this.priority = config.getInt("priority");
    }

    @Override
    public void addReplacements(Map<String, String> replacements) {
        super.addReplacements(replacements);
        replacements.put("{spawn}", this.name);
        replacements.put("{priority}", String.valueOf(this.priority));
    }

    public int getPriority() {
        return this.priority;
    }

    public void setNBTLocation(NbtCompound nbtCompound) {
        if (nbtCompound == null) nbtCompound = new NbtCompound();
        nbtCompound.putString("Dimension", this.getWorld().getRegistryKey().getValue().toString());
        NbtList listTag = new NbtList();
        listTag.addElement(0, NbtDouble.of(this.getX()));
        listTag.addElement(1, NbtDouble.of(this.getY()));
        listTag.addElement(2, NbtDouble.of(this.getZ()));
        nbtCompound.put("Pos", listTag);
    }

    @Override
    public Configuration toConfiguration() {
        Configuration locationConfig = super.toConfiguration();
        locationConfig.set("priority", this.priority);
        return locationConfig;
    }
}
