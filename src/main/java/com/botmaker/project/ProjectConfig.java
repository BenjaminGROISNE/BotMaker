package com.botmaker.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for BotMaker projects, stored in projects/.botmaker-config.json
 */
public class ProjectConfig {

    private static final Path CONFIG_FILE = Paths.get("projects/.botmaker-config.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private String lastOpenedProject;
    private List<ProjectEntry> recentProjects;

    public ProjectConfig() {
        this.recentProjects = new ArrayList<>();
    }

    public String getLastOpenedProject() {
        return lastOpenedProject;
    }

    public void setLastOpenedProject(String projectName) {
        this.lastOpenedProject = projectName;
    }

    public List<ProjectEntry> getRecentProjects() {
        return recentProjects;
    }

    public void addRecentProject(String projectName) {
        // Remove if already exists
        recentProjects.removeIf(p -> p.getName().equals(projectName));

        // Add at the beginning
        ProjectEntry entry = new ProjectEntry(projectName);
        recentProjects.add(0, entry);

        // Keep only last 10 projects
        if (recentProjects.size() > 10) {
            recentProjects = recentProjects.subList(0, 10);
        }
    }

    /**
     * Loads the configuration from disk
     */
    public static ProjectConfig load() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE);
                return GSON.fromJson(json, ProjectConfig.class);
            }
        } catch (Exception e) {
            System.err.println("Failed to load project config: " + e.getMessage());
        }

        // Return default config if file doesn't exist or can't be read
        return new ProjectConfig();
    }

    /**
     * Saves the configuration to disk
     */
    public void save() {
        try {
            // Ensure projects directory exists
            Files.createDirectories(CONFIG_FILE.getParent());

            // Write JSON
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_FILE, json);
        } catch (IOException e) {
            System.err.println("Failed to save project config: " + e.getMessage());
        }
    }

    /**
     * Updates the last opened project and saves
     */
    public static void updateLastOpened(String projectName) {
        ProjectConfig config = load();
        config.setLastOpenedProject(projectName);
        config.addRecentProject(projectName);
        config.save();
    }

    /**
     * Gets the last opened project name, or null if none
     */
    public static String getLastOpened() {
        ProjectConfig config = load();
        return config.getLastOpenedProject();
    }

    /**
     * Represents a recent project entry
     */
    public static class ProjectEntry {
        private String name;
        private String lastOpened; // ISO-8601 timestamp string

        public ProjectEntry() {
            // For Gson
        }

        public ProjectEntry(String name) {
            this.name = name;
            this.lastOpened = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLastOpened() {
            return lastOpened;
        }

        public void setLastOpened(String lastOpened) {
            this.lastOpened = lastOpened;
        }

        /**
         * Gets the last opened time as LocalDateTime
         */
        public LocalDateTime getLastOpenedDateTime() {
            try {
                return LocalDateTime.parse(lastOpened, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }
    }
}