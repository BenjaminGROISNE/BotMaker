package com.botmaker.library.emulator;

import com.android.ddmlib.IDevice;

import java.nio.file.Path;

public abstract class Emulator {

    protected final String name;
    protected final Path installPath;
    protected IDevice device;

    public Emulator(String name, Path installPath) {
        this.name = name;
        this.installPath = installPath;
    }

    public abstract void start() throws Exception;
    public abstract void connect() throws Exception;

    public boolean isGameInstalled(String packageName) throws Exception {
        if (device == null) {
            throw new IllegalStateException("Emulator not connected.");
        }
        return AdbHelper.isPackageInstalled(device, packageName);
    }

    public void launchGame(String packageName, String gameActivity) throws Exception {
        if (device == null) {
            throw new IllegalStateException("Emulator not connected.");
        }
        AdbHelper.launchGame(device, packageName, gameActivity);
    }

    public String getName() {
        return name;
    }

    public Path getInstallPath() {
        return installPath;
    }

    public IDevice getDevice() {
        return device;
    }

    public abstract String getMainWindowTitle();
    public abstract void takeScreenshot(Path outputPath) throws Exception;
}
