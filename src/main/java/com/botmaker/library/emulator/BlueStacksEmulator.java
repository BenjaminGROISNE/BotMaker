package com.botmaker.library.emulator;

import com.android.ddmlib.CollectingOutputReceiver;

import com.botmaker.library.inspector.RegistryInspector;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlueStacksEmulator extends Emulator {

    private static final String EMULATOR_NAME = "BlueStacks";
    private static final String BLUESTACKS_EXE = "HD-Player.exe";
    private static final String BLUESTACKS_CONF = "bluestacks.conf";

    private static final int MAX_SCREENSHOT_RETRIES = 5;
    private static final long SCREENSHOT_RETRY_DELAY_MS = 2000; // 2 seconds

    private final BlueStacksInstance instance;

    public BlueStacksEmulator(BlueStacksInstance instance) {
        super(EMULATOR_NAME, Paths.get(RegistryInspector.getBlueStacksInstallPath()));
        if (installPath == null) {
            throw new IllegalStateException("BlueStacks installation path not found.");
        }
        this.instance = instance;
    }

    @Override
    public void start() throws IOException {
        Path bluestacksExePath = installPath.resolve(BLUESTACKS_EXE);
        ProcessBuilder pb = new ProcessBuilder(bluestacksExePath.toString(), "--instance", instance.getInstanceName());
        pb.start();
    }

    @Override
    public void connect() throws Exception {
        String userDefinedDir = RegistryInspector.getBlueStacksUserDefinedDir();
        if (userDefinedDir == null) {
            throw new IllegalStateException("BlueStacks UserDefinedDir not found.");
        }
        BlueStacksConfig config = new BlueStacksConfig(Paths.get(userDefinedDir).resolve(BLUESTACKS_CONF));
        String adbPort = config.getAdbPort(instance.getInstanceName());
        if (adbPort == null) {
            throw new IllegalStateException("ADB port not found in bluestacks.conf for instance: " + instance.getInstanceName());
        }

        this.device = AdbHelper.connect("127.0.0.1", Integer.parseInt(adbPort));
        if (this.device == null) {
            throw new IllegalStateException("Failed to connect to BlueStacks ADB for instance: " + instance.getInstanceName());
        }
        AdbHelper.waitForBootComplete(this.device,120_000);
    }

    @Override
    public String getMainWindowTitle() {
        // This might need to be more dynamic, but for now, a common title
        return "BlueStacks App Player";
    }

    @Override
    public void takeScreenshot(Path outputPath) throws Exception {
        if (device == null) {
            throw new IllegalStateException("Emulator not connected.");
        }

        String devicePath = "/sdcard/screenshot.png";
        CollectingOutputReceiver receiver = new CollectingOutputReceiver();

        // Take screenshot on device
        device.executeShellCommand("screencap -p " + devicePath, receiver);

        // Pull the file from device
        device.pullFile(devicePath, outputPath.toString());

        // Delete the temporary file on device
        device.executeShellCommand("rm " + devicePath, receiver);
    }
}
