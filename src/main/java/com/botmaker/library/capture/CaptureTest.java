package com.botmaker.library.capture;


import com.botmaker.library.opencv.*;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static com.botmaker.library.capture.ScreenCapture.bufferedImageToMat;
import static com.botmaker.library.capture.ScreenCapture.matToBufferedImage;


public class CaptureTest {

    public static void main(String[] args){
        try {
            testPostLeftClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testLiveCapture() throws InterruptedException {
        User32.INSTANCE.SetProcessDPIAware();

        System.out.println("You have 5 seconds to bring the window you want to capture to the foreground...");
        Thread.sleep(5000);

        HWND selectedWindow = User32.INSTANCE.GetForegroundWindow();

        if (selectedWindow == null) {
            System.out.println("Could not get the foreground window.");
            return;
        }

        byte[] windowText = new byte[512];
        User32.INSTANCE.GetWindowTextA(selectedWindow.getPointer(), windowText, 512);
        String windowTitle = new String(windowText).trim();

        System.out.println("Capturing window: " + windowTitle);


        BufferedImage firstCapture = ScreenCapture.capture(selectedWindow);
        try {
            ImageIO.write(firstCapture, "png", new File("capture.png"));
            System.out.println("Saved the first capture to capture.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageDisplay display = new ImageDisplay();
        display.showImage(firstCapture);

        while (true) {
            BufferedImage screenshot = ScreenCapture.capture(selectedWindow);
            display.showImage(screenshot);
            Thread.sleep(100); // Capture roughly 10 times per second
        }
    }

    public static void testCreateTemplateAndFind() throws IOException, InterruptedException {
        System.out.println("Press 'C' to capture the first corner of the rectangle.");
        waitForCKey();
        Point p1 = getMousePosition();
        System.out.println("First corner captured at: " + p1);

        System.out.println("Press 'C' again to capture the second corner.");
        waitForCKey();
        Point p2 = getMousePosition();
        System.out.println("Second corner captured at: " + p2);

        Rectangle rect = new Rectangle(p1);
        rect.add(p2);

        BufferedImage desktop = ScreenCapture.captureDesktop();
        if (desktop == null) {
            System.err.println("Failed to capture desktop.");
            return;
        }

        BufferedImage templateImage = desktop.getSubimage(rect.x, rect.y, rect.width, rect.height);

        Template backgroundTemplate = new Template(bufferedImageToMat(desktop), "background");
        Template template = new Template(bufferedImageToMat(templateImage), "template");

        MatchResult result = OpencvManager.findBestMatch(template, backgroundTemplate, MatType.COLOR);

        if (result != null) {
            System.out.println("Match found at: " + result.rectLocation);
            System.out.println("Confidence: " + result.getScore());

            // Draw the rectangle on the background image
            Mat drawnImage = OpencvManager.drawMatch(backgroundTemplate.mat, result, new Scalar(0, 255, 0));

            // Convert the Mat back to a BufferedImage
            BufferedImage resultImage = matToBufferedImage(drawnImage);

            // Display the result
            ImageDisplay display = new ImageDisplay();
            display.showImage(resultImage);
        } else {
            System.out.println("No match found.");
        }
    }

    private static void waitForCKey() throws InterruptedException {
        // Wait for key release first to avoid capturing a held-down key
        while ((User32.INSTANCE.GetAsyncKeyState('C') & 0x8000) != 0) {
            Thread.sleep(10);
        }
        // Now wait for a fresh key press
        while ((User32.INSTANCE.GetAsyncKeyState('C') & 0x8000) == 0) {
            Thread.sleep(10);
        }
        System.out.println("... 'C' key pressed!");
    }

    private static Point getMousePosition() {
        WinDef.POINT p = new WinDef.POINT();
        User32.INSTANCE.GetCursorPos(p);
        return new Point(p.x, p.y);
    }

    public static void testCaptureChildWindow() throws InterruptedException {
        System.out.println("You have 5 seconds to bring the window you want to capture to the foreground...");
        Thread.sleep(5000);

        HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
        byte[] windowText = new byte[512];
        User32.INSTANCE.GetWindowTextA(foregroundWindow.getPointer(), windowText, 512);
        String windowTitle = new String(windowText).trim();

        List<WindowInfo> childWindows = WindowFinder.getChildWindows(foregroundWindow);

        System.out.println("Available windows to capture:");
        System.out.println("0: " + windowTitle + " (Parent)");
        for (int i = 0; i < childWindows.size(); i++) {
            System.out.println((i + 1) + ": " + childWindows.get(i).getTitle());
        }

        System.out.print("Enter the number of the window to capture: ");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        HWND selectedWindow;
        if (choice == 0) {
            selectedWindow = foregroundWindow;
        } else if (choice > 0 && choice <= childWindows.size()) {
            selectedWindow = childWindows.get(choice - 1).getHWnd();
        } else {
            System.out.println("Invalid choice.");
            return;
        }
        BufferedImage image = ScreenCapture.capture(selectedWindow);
        try {
            ImageIO.write(image, "png", new File("capture.png"));
            System.out.println("Saved the first capture to capture.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageDisplay display = new ImageDisplay();
        while (true) {
            BufferedImage screenshot = ScreenCapture.capture(selectedWindow);
            display.showImage(screenshot);
        }
    }

    public static void testPostLeftClick() throws InterruptedException {
        System.out.println("You have 5 seconds to bring the window you want to click on (using PostMessage) to the foreground...");
        Thread.sleep(5000);

        HWND selectedWindow = User32.INSTANCE.GetForegroundWindow();

        if (selectedWindow == null) {
            System.out.println("Could not get the foreground window.");
            return;
        }

        byte[] windowText = new byte[512];
        User32.INSTANCE.GetWindowTextA(selectedWindow.getPointer(), windowText, 512);
        String windowTitle = new String(windowText).trim();

        System.out.println("Move your mouse to the desired click location within the window and press 'C'.");
        waitForCKey();
        Point mousePos = getMousePosition();

        WinDef.RECT windowRect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(selectedWindow.getPointer(), windowRect);

        int relativeX = mousePos.x - windowRect.left;
        int relativeY = mousePos.y - windowRect.top;

        System.out.println("Clicking on window (PostMessage): " + windowTitle + " at relative coordinates (" + relativeX + ", " + relativeY + ") in 3 seconds...");
        Thread.sleep(3000);
        Clicker.postLeftClick(selectedWindow, relativeX, relativeY);
        System.out.println("Clicked!");
    }

    public static void testClickOnVirtualScreen() throws InterruptedException {

        System.out.println("Place le curseur, appuie sur 'C'");
        waitForCKey();
        Point p = getMousePosition();          // GetCursorPos → virt-desktop
        Clicker.postLeftClickScreen(p.x, p.y);
        System.out.println("Click envoyé à ("+p.x+", "+p.y+")");

    }
}