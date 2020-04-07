package com.diluv.diluvgradle.tests;

import java.io.File;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Tests {
    
    @Test
    public void testPluginAdded () {
        
        final GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(new File("src/test/resources/uploadtest"));
        
        try (final ProjectConnection connection = connector.connect()) {
            
            final BuildLauncher launcher = connection.newBuild();
            launcher.forTasks("testPlugin");
            launcher.run();
        }
        
        catch (final Exception e) {
            
            e.printStackTrace();
            Assertions.fail(e);
        }
    }
}