package me.neovitalism.neospawnpoints.spawnpoints;

import me.neovitalism.neoapi.modloading.config.Configuration;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class SpawnPoint extends Location {
    private final String name;
    private final int priority;

    public SpawnPoint(String name, ServerWorld world, double x, double y, double z, float pitch, float yaw, int priority) {
        super(world, x, y, z, pitch, yaw);
        this.name = name;
        this.priority = priority;
    }

    @Override
    public Configuration toConfiguration() {
        Configuration locationConfig = super.toConfiguration();
        locationConfig.set("priority", priority);
        return locationConfig;
    }

    @Override
    public void addReplacements(Map<String, String> replacements) {
        super.addReplacements(replacements);
        replacements.put("{spawn}", name);
        replacements.put("{priority}", String.valueOf(priority));
    }

    public int getPriority() {
        return priority;
    }
}
