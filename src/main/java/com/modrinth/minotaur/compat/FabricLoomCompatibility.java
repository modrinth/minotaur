package com.modrinth.minotaur.compat;

import com.modrinth.minotaur.Minotaur;
import net.fabricmc.loom.configuration.DependencyInfo;
import net.fabricmc.loom.util.Constants;

/**
 * All the utility methods for compatibility with Fabric Loom.
 */
public class FabricLoomCompatibility {
    /**
     * Detects the game version being used by Fabric Loom.
     *
     * @return The version of Minecraft that Loom is building against
     */
    public static String detectGameVersion() {
        return DependencyInfo.create(Minotaur.project, Constants.Configurations.MINECRAFT).getDependency().getVersion();
    }
}
