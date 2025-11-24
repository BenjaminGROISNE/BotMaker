package com.botmaker.library.capture;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

public class WindowInfo {

    private final HWND hWnd;
    private final String title;

    public WindowInfo(HWND hWnd, String title) {
        this.hWnd = hWnd;
        this.title = title;
    }

    public HWND getHWnd() {
        return hWnd;
    }

    public String getTitle() {
        return title;
    }

    public RECT getWindowRect() {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd.getPointer(), rect);
        return rect;
    }
}
