package com.botmaker.library.emulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BlueStacksConfig {

    private final Map<String, String> properties = new HashMap<>();

    public BlueStacksConfig(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    properties.put(parts[0].trim(), parts[1].trim().replace("\"", ""));
                }
            }
        }
    }


    public String getProperty(String key) {
        return properties.get(key);
    }

    public String getAdbPort(String instanceName) {
        return getProperty("bst.instance." + instanceName + ".adb_port");
    }
}
