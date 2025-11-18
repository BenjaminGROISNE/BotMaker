package com.botmaker.project;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Information about a project
 */
public class ProjectInfo {
    private final String name;
    private final Path projectPath;
    private final LocalDateTime lastModified;

    public ProjectInfo(String name, Path projectPath, LocalDateTime lastModified) {
        this.name = name;
        this.projectPath = projectPath;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public Path getProjectPath() {
        return projectPath;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return name;
    }
}