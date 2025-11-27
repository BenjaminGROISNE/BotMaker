package com.botmaker.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ApplicationConfig {

    // ... [Existing Fields remain same] ...
    private final Path jdtServerPath;
    private final Path projectPath;
    private final Path workspaceDataPath;
    private final Path sourceFilePath;
    private final Path compiledOutputPath;
    private final String mainClassName;
    private final String javaHome;
    private final String javaExecutable;
    private final String javacExecutable;
    private final int initialWidth;
    private final int initialHeight;
    private final boolean enableEventLogging;

    private ApplicationConfig(Builder builder) {
        this.jdtServerPath = builder.jdtServerPath;
        this.projectPath = builder.projectPath;
        this.workspaceDataPath = builder.workspaceDataPath;
        this.sourceFilePath = builder.sourceFilePath;
        this.compiledOutputPath = builder.compiledOutputPath;
        this.mainClassName = builder.mainClassName;
        this.javaHome = builder.javaHome;
        this.javaExecutable = builder.javaExecutable;
        this.javacExecutable = builder.javacExecutable;
        this.initialWidth = builder.initialWidth;
        this.initialHeight = builder.initialHeight;
        this.enableEventLogging = builder.enableEventLogging;
    }

    // ... [Getters remain same] ...
    public Path getJdtServerPath() { return jdtServerPath; }
    public Path getProjectPath() { return projectPath; }
    public Path getWorkspaceDataPath() { return workspaceDataPath; }
    public Path getSourceFilePath() { return sourceFilePath; }
    public Path getCompiledOutputPath() { return compiledOutputPath; }
    public String getMainClassName() { return mainClassName; }
    public String getJavaHome() { return javaHome; }
    public String getJavaExecutable() { return javaExecutable; }
    public String getJavacExecutable() { return javacExecutable; }
    public int getInitialWidth() { return initialWidth; }
    public int getInitialHeight() { return initialHeight; }
    public boolean isEnableEventLogging() { return enableEventLogging; }

    public static ApplicationConfig forProject(String projectName) {
        String javaHome = System.getProperty("java.home");
        String packageName = projectName.toLowerCase();
        String mainClassName = projectName;

        // --- FIX: OS Detection for Executables ---
        boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        String javaBin = isWindows ? "java.exe" : "java";
        String javacBin = isWindows ? "javac.exe" : "javac";

        return new Builder()
                .jdtServerPath(Paths.get("tools", "jdt-language-server")) // Use varargs for separator safety
                .projectPath(Paths.get("projects", projectName))
                .workspaceDataPath(Paths.get(System.getProperty("user.home"), ".jdtls-workspace", projectName))
                .sourceFilePath(Paths.get("projects", projectName, "src", "main", "java", "com", packageName, mainClassName + ".java"))
                .compiledOutputPath(Paths.get("projects", projectName, "build", "classes", "java", "main"))
                .mainClassName("com." + packageName + "." + mainClassName)
                .javaHome(javaHome)
                .javaExecutable(Paths.get(javaHome, "bin", javaBin).toString())
                .javacExecutable(Paths.get(javaHome, "bin", javacBin).toString())
                .initialWidth(600)
                .initialHeight(800)
                .enableEventLogging(false)
                .build();
    }

    // ... [Builder Class remains same] ...
    public static class Builder {
        private Path jdtServerPath;
        private Path projectPath;
        private Path workspaceDataPath;
        private Path sourceFilePath;
        private Path compiledOutputPath;
        private String mainClassName;
        private String javaHome;
        private String javaExecutable;
        private String javacExecutable;
        private int initialWidth = 600;
        private int initialHeight = 800;
        private boolean enableEventLogging = false;

        public Builder jdtServerPath(Path jdtServerPath) { this.jdtServerPath = jdtServerPath; return this; }
        public Builder projectPath(Path projectPath) { this.projectPath = projectPath; return this; }
        public Builder workspaceDataPath(Path workspaceDataPath) { this.workspaceDataPath = workspaceDataPath; return this; }
        public Builder sourceFilePath(Path sourceFilePath) { this.sourceFilePath = sourceFilePath; return this; }
        public Builder compiledOutputPath(Path compiledOutputPath) { this.compiledOutputPath = compiledOutputPath; return this; }
        public Builder mainClassName(String mainClassName) { this.mainClassName = mainClassName; return this; }
        public Builder javaHome(String javaHome) { this.javaHome = javaHome; return this; }
        public Builder javaExecutable(String javaExecutable) { this.javaExecutable = javaExecutable; return this; }
        public Builder javacExecutable(String javacExecutable) { this.javacExecutable = javacExecutable; return this; }
        public Builder initialWidth(int initialWidth) { this.initialWidth = initialWidth; return this; }
        public Builder initialHeight(int initialHeight) { this.initialHeight = initialHeight; return this; }
        public Builder enableEventLogging(boolean enableEventLogging) { this.enableEventLogging = enableEventLogging; return this; }

        public ApplicationConfig build() {
            if (jdtServerPath == null) throw new IllegalStateException("jdtServerPath is required");
            if (projectPath == null) throw new IllegalStateException("projectPath is required");
            if (mainClassName == null) throw new IllegalStateException("mainClassName is required");
            return new ApplicationConfig(this);
        }
    }
}