package com.botmaker.library.emulator;



import com.botmaker.library.inspector.RegistryInspector;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EmulatorTest {

    public static void main(String[] args) {
        AdbHelper.initAdb(); // Initialize ADB once
        try {
            String bluestacksInstallPathStr = RegistryInspector.getBlueStacksInstallPath();
            if (bluestacksInstallPathStr == null) {
                System.out.println("BlueStacks not found.");
                return;
            }
            Path bluestacksInstallPath = Paths.get(bluestacksInstallPathStr);
            System.out.println("BlueStacks Install Path: " + bluestacksInstallPath);

            String bluestacksDataDirStr = RegistryInspector.getBlueStacksDataDir();
            if (bluestacksDataDirStr == null) {
                System.out.println("BlueStacks Data Directory not found.");
                return;
            }
            Path bluestacksDataDir = Paths.get(bluestacksDataDirStr);
            System.out.println("BlueStacks Data Directory: " + bluestacksDataDir);

            Path mimMetaDataPath = bluestacksDataDir.resolve("UserData").resolve("MimMetaData.json");
            if (!mimMetaDataPath.toFile().exists()) {
                System.out.println("MimMetaData.json not found at: " + mimMetaDataPath);
                return;
            }

            List<BlueStacksInstance> instances = BlueStacksInstanceManager.getInstances(mimMetaDataPath);
            if (instances.isEmpty()) {
                System.out.println("No BlueStacks instances found.");
                return;
            }

            System.out.println("Found BlueStacks Instances:");
            for (BlueStacksInstance instance : instances) {
                System.out.println("  ID: " + instance.getId() + ", Name: " + instance.getName() + ", InstanceName: " + instance.getInstanceName());

                BlueStacksEmulator emulator = new BlueStacksEmulator(instance);
                System.out.println("  Attempting to start emulator: " + emulator.getName() + " (" + emulator.getInstallPath() + ")");
                emulator.start();
                System.out.println("  Emulator started. Attempting to connect to ADB...");
                emulator.connect();

                if (emulator.getDevice() != null) {
                    System.out.println("  Connected to ADB device: " + emulator.getDevice().getSerialNumber());


                    String gamePackage = "com.supercell.clashofclans"; // Replace with your game's package name
                    if (emulator.isGameInstalled(gamePackage)) {
                        System.out.println("  Game " + gamePackage + " is installed.");
                        String gameActivity = "com.supercell.titan.GameApp"; // Replace with your game's main activity
                        emulator.launchGame(gamePackage, gameActivity);
                        System.out.println("  Game launched.");
                    } else {
                        System.out.println("  Game " + gamePackage + " is NOT installed.");
                    }

                    System.out.println("  Main Window Title: " + emulator.getMainWindowTitle());

                    // Take a screenshot
                    Path screenshotPath = Paths.get("screenshot_" + instance.getInstanceName() + ".png");
                    emulator.takeScreenshot(screenshotPath);
                    System.out.println("  Screenshot saved to: " + screenshotPath.toAbsolutePath());

                } else {
                    System.out.println("  Failed to connect to ADB for this instance.");
                }
                System.out.println("----------------------------------------");
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            AdbHelper.terminateAdb(); // Terminate ADB when done
        }
    }
}
