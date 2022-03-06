package com.modrinth.minotaur.compat;

import net.fabricmc.loom.configuration.DependencyInfo;
import net.fabricmc.loom.util.Constants;
import org.gradle.api.Project;

/**
 * All the utility methods for compatibility with Fabric Loom.
 */
public class FabricLoomCompatibility {
    /**
     * Detects the game version being used by Fabric Loom.
     * @param project The Gradle project that Minotaur is applied to
     * @return The version of Minecraft that Loom is building against
     */
    public static String detectGameVersion(final Project project) {
        return DependencyInfo.create(project, Constants.Configurations.MINECRAFT).getDependency().getVersion();
    }
}
