package com.botmaker.library.capture;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ImageDisplay {
    private final JFrame frame;
    private final JLabel imageLabel;

    public ImageDisplay() {
        frame = new JFrame("Live Capture");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        imageLabel = new JLabel();
        frame.getContentPane().add(imageLabel);
        frame.setResizable(false);
    }

    public void showImage(BufferedImage image) {
        if (image == null) {
            return;
        }
        imageLabel.setIcon(new ImageIcon(image));
        if (!frame.isVisible()) {
            frame.pack();
            frame.setVisible(true);
        }
    }
}
