package com.botmaker.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manages project discovery and listing
 */
public class ProjectManager {

    private static final Path PROJECTS_ROOT = Paths.get("projects");

    /**
     * Lists all available projects
     */
    public List<ProjectInfo> listProjects() {
        List<ProjectInfo> projects = new ArrayList<>();

        if (!Files.exists(PROJECTS_ROOT)) {
            return projects;
        }

        try (Stream<Path> paths = Files.list(PROJECTS_ROOT)) {
            paths.filter(Files::isDirectory)
                    .filter(this::isValidProject)
                    .forEach(projectPath -> {
                        try {
                            String projectName = projectPath.getFileName().toString();
                            FileTime lastModified = Files.getLastModifiedTime(projectPath);
                            LocalDateTime modifiedDate = LocalDateTime.ofInstant(
                                    lastModified.toInstant(),
                                    ZoneId.systemDefault()
                            );
                            projects.add(new ProjectInfo(projectName, projectPath, modifiedDate));
                        } catch (IOException e) {
                            System.err.println("Error reading project: " + projectPath);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing projects: " + e.getMessage());
        }

        return projects;
    }

    /**
     * Checks if a directory is a valid project
     * (has src/main/java structure and build.gradle)
     */
    private boolean isValidProject(Path projectPath) {
        Path srcPath = projectPath.resolve("src/main/java");
        Path buildGradle = projectPath.resolve("build.gradle");
        boolean isValid = Files.exists(srcPath) && Files.exists(buildGradle);

        // Debug output
        System.out.println("Checking project: " + projectPath);
        System.out.println("  src/main/java exists: " + Files.exists(srcPath));
        System.out.println("  build.gradle exists: " + Files.exists(buildGradle));
        System.out.println("  Valid: " + isValid);

        return isValid;
    }

    /**
     * Gets the source file path for a project
     */
    public Path getSourceFilePath(String projectName) {
        String packageName = projectName.toLowerCase();
        return PROJECTS_ROOT
                .resolve(projectName)
                .resolve("src/main/java/com")
                .resolve(packageName)
                .resolve(projectName + ".java");
    }
}