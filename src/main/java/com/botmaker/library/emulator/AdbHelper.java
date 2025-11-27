package com.botmaker.library.emulator;

import com.android.ddmlib.*;

import java.io.IOException;

public class AdbHelper {

    private static AndroidDebugBridge adb;

    public static void initAdb() {
        if (adb == null) {
            AndroidDebugBridge.init(false);
            adb = AndroidDebugBridge.createBridge();
        }
    }

    public static void terminateAdb() {
        if (adb != null) {
            AndroidDebugBridge.terminate();
            adb = null;
        }
    }

    public static IDevice connect(String adbHost, int adbPort) throws Exception {
        String serial = adbHost + ":" + adbPort;

        // First, check if the device is already connected
        for (IDevice device : adb.getDevices()) {
            if (device.getSerialNumber().equals(serial)) {
                return device;
            }
        }

        // If not, try to connect to it via adb command
        System.out.println("Attempting to connect to ADB device: " + serial);
        ProcessBuilder pb = new ProcessBuilder("adb", "connect", serial);
        Process process = pb.start();
        process.waitFor(); // Wait for the adb connect command to finish

        long startTime = System.currentTimeMillis();
        long timeout = 30 * 1000; // 30 seconds timeout

        while (System.currentTimeMillis() - startTime < timeout) {
            for (IDevice device : adb.getDevices()) {
                if (device.getSerialNumber().equals(serial)) {
                    System.out.println("Successfully connected to ADB device: " + serial);
                    return device;
                }
            }
            Thread.sleep(1000); // Wait for 1 second before retrying
        }
        System.out.println("Failed to connect to ADB device: " + serial + " after timeout.");
        return null;
    }

    public static boolean isPackageInstalled(IDevice device, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        CollectingOutputReceiver receiver = new CollectingOutputReceiver();
        device.executeShellCommand("pm list packages " + packageName, receiver);
        return receiver.getOutput().contains(packageName);
    }

    public static void launchGame(IDevice device, String packageName, String activityName) throws AdbCommandRejectedException, TimeoutException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand("am start -n " + packageName + "/" + activityName, new CollectingOutputReceiver());
    }

    public static void waitForBootComplete(IDevice dev, long timeoutMs)
            throws TimeoutException, AdbCommandRejectedException, IOException, InterruptedException {

        long deadline = System.currentTimeMillis() + timeoutMs;


        while (dev.getState() != IDevice.DeviceState.ONLINE) {
            if (System.currentTimeMillis() > deadline) {
                throw new TimeoutException("Device never came online");
            }
            Thread.sleep(1_000);
        }


        while (true) {
            String boot = dev.getProperty("sys.boot_completed");            // shortcut wrapper
            String anim = dev.getProperty("init.svc.bootanim");             // may be null on -no-boot-anim
            if ("1".equals(boot) && (anim == null || "stopped".equals(anim))) {
                return;                                                     // fully booted
            }
            if (System.currentTimeMillis() > deadline) {
                throw new TimeoutException("Device did not finish booting");
            }
            Thread.sleep(1_000);
        }
    }


}

