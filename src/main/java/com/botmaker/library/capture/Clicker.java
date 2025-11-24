package com.botmaker.library.capture;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.WPARAM;

public final class Clicker {

    private static LPARAM makeLParam(int x, int y) {
        return new LPARAM((y << 16) | (x & 0xFFFF));
    }



    public static void postLeftClickScreen(int xAbs, int yAbs) {

        // 1️⃣ quelle fenêtre est sous ce pixel ?
        POINT.ByValue ptScreen = new POINT.ByValue();
        ptScreen.x = xAbs;
        ptScreen.y = yAbs;

        HWND hWnd = User32.INSTANCE.WindowFromPoint(ptScreen);
        if (hWnd == null) {
            return;
        }

        POINT ptClient = new POINT();
        ptClient.x = xAbs;
        ptClient.y = yAbs;
        User32.INSTANCE.ScreenToClient(hWnd, ptClient);

        // 3️⃣ réutiliser le helper qui marche
        postLeftClick(hWnd, ptClient.x, ptClient.y);
    }


    /** Posts a left click at (x,y) given in *client* coords of hWnd. */
    public static void postLeftClick(HWND hWnd, int x, int y) {

        /* 1️⃣ client → screen */
        POINT ptScreen = new POINT();
        ptScreen.x = x;
        ptScreen.y = y;
        User32.INSTANCE.ClientToScreen(hWnd, ptScreen);

        /* 2️⃣ window really under that point (child or same) */
        HWND hTarget = User32.INSTANCE.WindowFromPoint(ptScreen);
        if (hTarget == null) {
            hTarget = hWnd;
        }

        /* 3️⃣ screen → client of that target */
        POINT ptClient = new POINT();
        ptClient.x = ptScreen.x;
        ptClient.y = ptScreen.y;
        User32.INSTANCE.ScreenToClient(hTarget, ptClient);

        /* 4️⃣ pack & shoot the messages */
        LPARAM lParam = makeLParam(ptClient.x, ptClient.y);
        WPARAM wDown  = new WPARAM(User32.MK_LBUTTON);

        User32.INSTANCE.PostMessage(hTarget, User32.WM_LBUTTONDOWN, wDown, lParam);
        User32.INSTANCE.PostMessage(hTarget, User32.WM_LBUTTONUP,   new WPARAM(0), lParam);
    }

    private Clicker() {}  // utility class
}