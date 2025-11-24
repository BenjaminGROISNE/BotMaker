package com.botmaker.project;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LibraryManager {

    // Map of Relative Path -> Source Code Content
    private static final Map<String, String> LIBRARY_FILES = new HashMap<>();

    // Load the library definition
    static {
        // 1. CAPTURE
        loadSource("capture/Clicker.java");
        loadSource("capture/GDI32.java");
        loadSource("capture/ImageDisplay.java");
        loadSource("capture/ScreenCapture.java");
        loadSource("capture/User32.java");
        loadSource("capture/WindowFinder.java");
        loadSource("capture/WindowInfo.java");

        // 2. EMULATOR
        loadSource("emulator/AdbHelper.java");
        loadSource("emulator/BlueStacksConfig.java");
        loadSource("emulator/BlueStacksEmulator.java");
        loadSource("emulator/BlueStacksInstance.java");
        loadSource("emulator/BlueStacksInstanceManager.java");
        loadSource("emulator/Emulator.java");

        // 3. INSPECTOR
        loadSource("inspector/RegistryInspector.java");

        // 4. INTERACTION
        loadSource("interaction/GameInteractor.java");
        loadSource("interaction/GameType.java");

        // 5. OPENCV
        loadSource("opencv/MatchResult.java");
        loadSource("opencv/MatType.java");
        loadSource("opencv/OpencvManager.java");
        loadSource("opencv/Template.java");

        // 6. LIBRARY ROOT
        loadSource("Main.java"); // The library Main class if needed
    }

    /**
     * Loads source code into memory.
     * Priority 1: Read from current IntelliJ Project (Dev Mode)
     * Priority 2: Read from JAR Resources (Production Mode)
     */
    private static void loadSource(String relativePath) {
        String content = null;
        try {
            // DEV MODE: Try reading directly from your source folder
            // This assumes BotMaker is running from within the project root
            Path devPath = Paths.get("src/main/java/com/botmaker/library", relativePath);
            if (Files.exists(devPath)) {
                content = Files.readString(devPath);
            } else {
                // PROD MODE: Read from classpath resources
                // You must ensure your build process copies these java files to src/main/resources/library_src/
                String resourcePath = "/library_src/" + relativePath;
                InputStream stream = LibraryManager.class.getResourceAsStream(resourcePath);
                if (stream != null) {
                    content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            if (content != null) {
                // Correct the package declaration if necessary, though usually it stays com.botmaker.library...
                LIBRARY_FILES.put(relativePath, content);
            } else {
                System.err.println("LibraryManager: Could not find source for " + relativePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Installs the library source code into the user's project.
     */
    public void installLibrary(Path projectPath) {
        try {
            Path libRoot = projectPath.resolve("src/main/java/com/botmaker/library");

            for (Map.Entry<String, String> entry : LIBRARY_FILES.entrySet()) {
                Path targetFile = libRoot.resolve(entry.getKey());
                Files.createDirectories(targetFile.getParent());

                // Only write if different to preserve file modification times if possible
                if (!Files.exists(targetFile) || !Files.readString(targetFile).equals(entry.getValue())) {
                    Files.writeString(targetFile, entry.getValue());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to install library: " + e.getMessage());
        }
    }

    /**
     * Checks if user modified library files and repairs them.
     * Returns true if repairs were made.
     */
    public boolean verifyAndRepair(Path projectPath) {
        Path libRoot = projectPath.resolve("src/main/java/com/botmaker/library");
        boolean repaired = false;

        try {
            for (Map.Entry<String, String> entry : LIBRARY_FILES.entrySet()) {
                Path targetFile = libRoot.resolve(entry.getKey());
                String expected = entry.getValue();

                if (!Files.exists(targetFile)) {
                    System.out.println("Restoring missing library file: " + entry.getKey());
                    Files.createDirectories(targetFile.getParent());
                    Files.writeString(targetFile, expected);
                    repaired = true;
                } else {
                    // Simple string comparison.
                    // In production, you might ignore whitespace or use a hash.
                    String current = Files.readString(targetFile);
                    // Normalize line endings
                    if (!current.replace("\r\n", "\n").trim().equals(expected.replace("\r\n", "\n").trim())) {
                        System.out.println("Repairing modified library file: " + entry.getKey());
                        Files.writeString(targetFile, expected);
                        repaired = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return repaired;
    }
}