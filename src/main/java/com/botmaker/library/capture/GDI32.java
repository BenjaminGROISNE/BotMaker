package com.botmaker.library.capture;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;


public interface GDI32 extends StdCallLibrary {
    GDI32 INSTANCE = Native.load("gdi32", GDI32.class);

    boolean BitBlt(HDC hdcDest, int nXDest, int nYDest, int nWidth, int nHeight, HDC hdcSrc, int nXSrc, int nYSrc, int dwRop);
    HDC CreateCompatibleDC(HDC hdc);
    HBITMAP CreateCompatibleBitmap(HDC hdc, int nWidth, int nHeight);
    Pointer SelectObject(HDC hdc, Pointer h);
    boolean DeleteDC(HDC hdc);
    boolean DeleteObject(HBITMAP ho);
    int GetObject(HANDLE h, int c, WinGDI.BITMAP lp);
    int GetDIBits(HDC hdc, HBITMAP hbm, int uStartScan, int cScanLines, int[] lpvBits, WinGDI.BITMAPINFO lpbi, int uUsage);
}