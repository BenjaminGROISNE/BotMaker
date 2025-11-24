package com.botmaker.library.opencv;


import com.botmaker.library.capture.ScreenCapture;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class OpencvTest {
    static{
        Loader.load(opencv_java.class);
    }
    private static final String BACKGROUNDS_DIR = "images/backgrounds";
    private static final String TEMPLATES_DIR = "images/templates";

    public static void main(String[] args) throws IOException, InterruptedException {
        // 1. Record screen and save backgrounds
        recordScreenAndSave(10, 2);

        // 2. Extract templates from backgrounds
        extractTemplatesFromBackgrounds(3);

        // 3. Benchmark OpenCV functions
        benchmarkOpenCVFunctions();
    }

    public static void recordScreenAndSave(int screenshotCount, int intervalSeconds) throws InterruptedException, IOException {
        File backgroundsDir = new File(BACKGROUNDS_DIR);
        if (!backgroundsDir.exists()) {
            backgroundsDir.mkdirs();
        }

        for (int i = 0; i < screenshotCount; i++) {
            BufferedImage screenshot = ScreenCapture.captureDesktop();
            if (screenshot != null) {
                File outputFile = new File(BACKGROUNDS_DIR + "/background_" + i + ".png");
                ImageIO.write(screenshot, "png", outputFile);
                System.out.println("Saved background image: " + outputFile.getAbsolutePath());
            }
            TimeUnit.SECONDS.sleep(intervalSeconds);
        }
    }

    public static void extractTemplatesFromBackgrounds(int templatesPerBackground) throws IOException {
        File templatesDir = new File(TEMPLATES_DIR);
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }

        File backgroundsDir = new File(BACKGROUNDS_DIR);
        File[] backgroundFiles = backgroundsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (backgroundFiles == null) {
            System.out.println("No background images found in " + BACKGROUNDS_DIR);
            return;
        }

        Random random = new Random();
        int templateCount = 0;
        for (File backgroundFile : backgroundFiles) {
            BufferedImage backgroundImage = ImageIO.read(backgroundFile);
            for (int i = 0; i < templatesPerBackground; i++) {
                // Random size between 5% and 20% of the background dimensions
                double scale = 0.05 + (0.15 * random.nextDouble());
                int templateWidth = (int) (backgroundImage.getWidth() * scale);
                int templateHeight = (int) (backgroundImage.getHeight() * scale);

                int x = random.nextInt(backgroundImage.getWidth() - templateWidth);
                int y = random.nextInt(backgroundImage.getHeight() - templateHeight);
                BufferedImage templateImage = backgroundImage.getSubimage(x, y, templateWidth, templateHeight);

                File outputFile = new File(TEMPLATES_DIR + "/template_" + templateCount++ + ".png");
                ImageIO.write(templateImage, "png", outputFile);
                System.out.println("Saved template image: " + outputFile.getAbsolutePath());
            }
        }
    }

    public static void benchmarkOpenCVFunctions() {
        File backgroundsDir = new File(BACKGROUNDS_DIR);
        File[] backgroundFiles = backgroundsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        File templatesDir = new File(TEMPLATES_DIR);
        File[] templateFiles = templatesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (backgroundFiles == null || templateFiles == null) {
            System.out.println("No background or template images found.");
            return;
        }

        List<Template> backgroundTemplates = new ArrayList<>();
        for (File file : backgroundFiles) {
            backgroundTemplates.add(new Template(file.getAbsolutePath()));
        }

        List<Template> templates = new ArrayList<>();
        for (File file : templateFiles) {
            templates.add(new Template(file.getAbsolutePath()));
        }

        System.out.println("--- Benchmarking Results ---");

        // --- Benchmarking findBestMatch ---
        long totalTimeBestMatch = 0;
        int bestMatchCount = 0;
        for (Template background : backgroundTemplates) {
            for (Template template : templates) {
                long startTime = System.nanoTime();
                MatchResult result = OpencvManager.findBestMatch(template, background, MatType.COLOR);
                long endTime = System.nanoTime();
                totalTimeBestMatch += (endTime - startTime);
                bestMatchCount++;
                if (result != null) {
                    System.out.printf("findBestMatch: Found %s in %s with score %.4f%n",
                            new File(template.id).getName(), new File(background.id).getName(), result.getScore());
                }
            }
        }
        System.out.println("findBestMatch average time: " + (totalTimeBestMatch / bestMatchCount) / 1_000_000.0 + " ms");


        long totalTimeMultipleMatches = 0;
        int multipleMatchesCount = 0;
        for (Template background : backgroundTemplates) {
            for (Template template : templates) {
                long startTime = System.nanoTime();
                List<MatchResult> results = OpencvManager.findMultipleMatches(template, background, MatType.COLOR);
                long endTime = System.nanoTime();
                totalTimeMultipleMatches += (endTime - startTime);
                multipleMatchesCount++;
                if (results != null && !results.isEmpty()) {
                    for (MatchResult result : results) {
                        System.out.printf("findMultipleMatches: Found %s in %s with score %.4f%n",
                                new File(template.id).getName(), new File(background.id).getName(), result.getScore());
                    }
                }
            }
        }
        System.out.println("findMultipleMatches average time: " + (totalTimeMultipleMatches / multipleMatchesCount) / 1_000_000.0 + " ms");

         long totalTimeBestMatchPerTemplate = 0;
        int bestMatchPerTemplateCount = 0;
        for (Template background : backgroundTemplates) {
            long startTime = System.nanoTime();
            List<MatchResult> results = OpencvManager.findBestMatchPerTemplate(templates, background, MatType.COLOR);
            long endTime = System.nanoTime();
            totalTimeBestMatchPerTemplate += (endTime - startTime);
            bestMatchPerTemplateCount++;
            if (results != null && !results.isEmpty()) {
                for (MatchResult result : results) {
                    System.out.printf("findBestMatchPerTemplate: Found match in %s with score %.4f%n",
                            new File(background.id).getName(), result.getScore());
                }
            }
        }
        System.out.println("findBestMatchPerTemplate average time: " + (totalTimeBestMatchPerTemplate / bestMatchPerTemplateCount) / 1_000_000.0 + " ms");
    }
}
