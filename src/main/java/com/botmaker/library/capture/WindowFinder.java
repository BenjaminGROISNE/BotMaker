package com.botmaker.library.capture;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.util.ArrayList;
import java.util.List;

public class WindowFinder {

    public static HWND findWindow(String title) {
        return new HWND(User32.INSTANCE.FindWindowA(null, title));
    }

    public static List<WindowInfo> getChildWindows(HWND parent) {
        List<WindowInfo> windows = new ArrayList<>();
        User32.INSTANCE.EnumChildWindows(parent, (hWnd, p) -> {
            byte[] windowText = new byte[512];
            User32.INSTANCE.GetWindowTextA(hWnd, windowText, 512);
            String title = new String(windowText).trim();
            if (!title.isEmpty()) {
                windows.add(new WindowInfo(new HWND(hWnd), title));
            }
            return true;
        }, null);
        return windows;
    }

    public static List<WindowInfo> getAllWindows() {
        List<WindowInfo> windows = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hWnd, p) -> {
            byte[] windowText = new byte[512];
            User32.INSTANCE.GetWindowTextA(hWnd, windowText, 512);
            String title = new String(windowText).trim();
            if (!title.isEmpty()) {
                windows.add(new WindowInfo(new HWND(hWnd), title));
            }
            return true;
        }, null);
        return windows;
    }
}
