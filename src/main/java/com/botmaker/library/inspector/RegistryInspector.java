package com.botmaker.library.inspector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RegistryInspector {

    public static String getLDPlayerInstallPath() {
        return getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\XuanZhi\\LDPlayer9", "InstallDir");
    }

    public static String getBlueStacksInstallPath() {
        String path = getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\BlueStacks_nxt", "InstallDir");
        if (path == null) {
            path = getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\BlueStacks_msi", "InstallDir");
        }
        return path;
    }

    public static String getBlueStacksDataDir() {
        String path = getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\BlueStacks_nxt", "DataDir");
        if (path == null) {
            path = getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\BlueStacks_msi", "DataDir");
        }
        return path;
    }

    public static String getBlueStacksUserDefinedDir() {
        String path = getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\BlueStacks_nxt", "UserDefinedDir");
        if (path == null) {
            path = getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\BlueStacks_msi", "UserDefinedDir");
        }
        return path;
    }

    private static String getRegistryValue(String key, String valueName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("reg", "query", key, "/v", valueName);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(valueName)) {
                    String[] parts = line.trim().split("    ");
                    if (parts.length > 1) {
                        return parts[parts.length - 1];
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("Checking for LDPlayer...");
        String ldPlayerPath = getLDPlayerInstallPath();
        if (ldPlayerPath != null) {
            System.out.println("LDPlayer is installed at: " + ldPlayerPath);
        } else {
            System.out.println("LDPlayer is not installed.");
        }

        System.out.println("\nChecking for BlueStacks...");
        String blueStacksPath = getBlueStacksInstallPath();
        if (blueStacksPath != null) {
            System.out.println("BlueStacks is installed at: " + blueStacksPath);
        } else {
            System.out.println("BlueStacks is not installed.");
        }
    }
}
