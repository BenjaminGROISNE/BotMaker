package com.botmaker.library.capture;

import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinGDI;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;


public class ScreenCapture {


    static Mat bufferedImageToMat(BufferedImage bi) {

        BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        convertedImg.getGraphics().drawImage(bi, 0, 0, null);

        Mat mat = new Mat(convertedImg.getHeight(), convertedImg.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    public static BufferedImage capture(HWND hWnd) {
        // Get screen dimensions for fullscreen check
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        RECT screenRect = new RECT();
        screenRect.left = 0;
        screenRect.top = 0;
        screenRect.right = (int) screenSize.getWidth();
        screenRect.bottom = (int) screenSize.getHeight();

        // Get window dimensions
        RECT windowRect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd.getPointer(), windowRect);

        BufferedImage image;

        // Check if the window is fullscreen. If so, Robot is more reliable.
        if (windowRect.toString().equals(screenRect.toString()) && User32.INSTANCE.GetForegroundWindow().equals(hWnd)) {
            image = captureWithRobot(hWnd, windowRect);
        } else {
            // For windowed mode, GDI is faster, with a fallback to Robot.
            image = captureWithGDI(hWnd);
            // Fallback for black, invalid, or frozen (stale) images.
            if (image == null || image.getWidth() == 0 || image.getHeight() == 0 || isBlack(image)) {
                image = captureWithRobot(hWnd, windowRect);
            }
        }
        return image;
    }

    private static BufferedImage captureWithGDI(HWND hWnd) {
        HDC hdcWindow = User32.INSTANCE.GetDC(hWnd);
        HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        RECT bounds = new RECT();
        User32.INSTANCE.GetClientRect(hWnd, bounds);

        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;

        if (width <= 0 || height <= 0) {
            User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);
            GDI32.INSTANCE.DeleteDC(hdcMemDC);
            return null;
        }

        HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);
        GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap.getPointer());

        User32.INSTANCE.PrintWindow(hWnd, hdcMemDC, 2); // 2 = PW_CLIENTONLY

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // Top-down image
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, ((DataBufferInt) image.getRaster().getDataBuffer()).getData(), bmi, WinGDI.DIB_RGB_COLORS);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);
        User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);

        return image;
    }

    private static BufferedImage captureWithRobot(HWND hWnd, RECT bounds) {
        int width = bounds.right - bounds.left;
        int height = bounds.bottom - bounds.top;
        if (width <= 0 || height <= 0) {
            // Before failing, try to get the bounds again, as the window might have been minimized.
            User32.INSTANCE.GetWindowRect(hWnd.getPointer(), bounds);
            width = bounds.right - bounds.left;
            height = bounds.bottom - bounds.top;
            if (width <= 0 || height <= 0) {
                return null;
            }
        }
        try {
            return new Robot().createScreenCapture(new Rectangle(bounds.left, bounds.top, width, height));
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isBlack(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        // Check a few pixels to see if they are black. A small sample is enough.
        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            if ((image.getRGB(x, y) & 0x00FFFFFF) != 0) {
                return false; // Found a non-black pixel
            }
        }
        return true;
    }

    public static BufferedImage captureDesktop() {
        try {
            return new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        }
    }
}
