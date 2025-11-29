package com.botmaker.library.capture;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.win32.StdCallLibrary;

public interface User32 extends StdCallLibrary {


    User32 INSTANCE = Native.load(
            "user32",
            User32.class,
            W32APIOptions.DEFAULT_OPTIONS);
    interface WNDENUMPROC extends StdCallCallback {
        boolean callback(Pointer hWnd, Pointer arg);
    }

    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
    boolean EnumChildWindows(HWND parent, WNDENUMPROC lpEnumFunc, Pointer arg);

    /* ---------  text / geometry  --------- */

    int  GetWindowTextA(Pointer hWnd, byte[] lpString, int nMax);
    Pointer FindWindowA(String lpClass, String lpName);
    boolean GetWindowRect(Pointer hWnd, RECT rect);
    boolean GetClientRect(HWND hWnd, RECT rect);

    /* ---------  DC / painting  --------- */

    HDC GetDC(HWND hWnd);
    int ReleaseDC(HWND hWnd, HDC hDC);
    boolean PrintWindow(HWND hWnd, HDC hdcBlt, int flags);

    /* ---------  DPI / focus / mouse pos  --------- */

    boolean SetProcessDPIAware();
    HWND    GetForegroundWindow();
    boolean SetForegroundWindow(HWND hWnd);
    boolean GetCursorPos(POINT pt);
    short   GetAsyncKeyState(int vKey);

    /* ---------  coordinate helpers  --------- */

    boolean ClientToScreen(HWND hWnd, POINT pt);
    boolean ScreenToClient(HWND hWnd, POINT pt);

    /* ---------  hit-testing  --------- */
    HWND WindowFromPoint(POINT pt);
    HWND WindowFromPoint(POINT.ByValue pt);   // ← ByValue !
    int  CWP_ALL = 0x0000;
    HWND ChildWindowFromPointEx(HWND parent, POINT pt, int flags);

    /* ---------  messaging  --------- */

    boolean PostMessage(HWND hWnd, int msg, WPARAM wp, LPARAM lp);
    LRESULT SendMessage(HWND hWnd, int msg, WPARAM wp, LPARAM lp);

    /* ---- NEW DESKTOP / METRICS ------------------------------------------- */

    HWND GetDesktopWindow();                // top level “Progman/WorkerW” window
    int  GetSystemMetrics(int index);       // screen size, virtual-desktop origin

    int SM_XVIRTUALSCREEN = 76;   // left   of bounding rect (can be negative)
    int SM_YVIRTUALSCREEN = 77;   // top    of bounding rect
    int SM_CXVIRTUALSCREEN = 78;  // width  of bounding rect
    int SM_CYVIRTUALSCREEN = 79;  // height of bounding rect

    HWND GetAncestor(HWND hWnd, int gaFlags);     // GA_ROOT = 2, GA_ROOTOWNER = 3
    boolean ShowWindow(HWND hWnd, int nCmdShow);  // SW_RESTORE = 9
    /* ---------  mouse constants  --------- */

    int WM_LBUTTONDOWN = 0x0201;
    int WM_LBUTTONUP   = 0x0202;
    int MK_LBUTTON     = 0x0001;
}
