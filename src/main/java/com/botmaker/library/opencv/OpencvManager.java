package com.botmaker.library.opencv;


import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.*;



public class OpencvManager {
    private static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.8;

    public static Mat bufferedImageToMat(BufferedImage image) {

        BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        convertedImg.getGraphics().drawImage(image, 0, 0, null);

        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);

        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();

        mat.put(0, 0, pixels);
        return mat;
    }

    public static Template loadTemplate(String path) {
        Mat mat = imread(path);
        if (mat.empty()) {
            System.err.println("Error: Could not load template from path: " + path);
            return null;
        }
        return new Template(mat, path);
    }

    public OpencvManager() {

    }

    static public Boolean isRGBA(Mat mat){
        return mat.channels() == 4;
    }

    static public Boolean isRGB(Mat mat){
        return mat.channels() == 3;
    }
    static public Boolean isGray(Mat mat){
        return mat.channels() == 1;
    }

    static void convertToBGR(Mat mat){
        if(isGray(mat)){
            cvtColor(mat, mat, COLOR_GRAY2RGB);
        }
        else if(isRGBA(mat)){
            cvtColor(mat, mat, COLOR_RGBA2RGB);
        }
    }

    static void convertToGray(Mat mat){
        if(isRGB(mat)){
            cvtColor(mat, mat, COLOR_RGB2GRAY);
        }
        else if(isRGBA(mat)){
            cvtColor(mat, mat, COLOR_RGBA2GRAY);
        }
    }
    public static Mat drawMatch(Mat image, MatchResult match, Scalar color) {
        Mat drawnImage = image.clone();
        Imgproc.rectangle(drawnImage, match.rectLocation, color, 2);
        return drawnImage;
    }
    static public MatchResult findBestMatch(Template template, Template backgroundTemplate,MatType convertType){
        return findBestMatch(template,backgroundTemplate,convertType,DEFAULT_CONFIDENCE_THRESHOLD);
    }

    static public void convertTo(Mat mat, MatType convertType){
        if(convertType == MatType.COLOR){
            convertToBGR(mat);
        }
        else if(convertType == MatType.GRAY){
            convertToGray(mat);
        }
    }

    static public void convertTo(Template template, MatType convertType){
        convertTo(template.mat,convertType);
    }



    private static Core.MinMaxLocResult performMatch(Mat templateMat, Mat backgroundMat) {
        if (backgroundMat.width() < templateMat.width() || backgroundMat.height() < templateMat.height()) {
            System.err.println("Error: Template dimensions are larger than the background image.");
            return null;
        }

        int resultCols = backgroundMat.cols() - templateMat.cols() + 1;
        int resultRows = backgroundMat.rows() - templateMat.rows() + 1;
        Mat resultMat = new Mat(resultRows, resultCols, CvType.CV_32FC1);

        Imgproc.matchTemplate(backgroundMat, templateMat, resultMat, Imgproc.TM_CCOEFF_NORMED);
        return Core.minMaxLoc(resultMat);
    }

    static public MatchResult findBestMatch(Template template, Template backgroundTemplate, MatType convertType, double confidenceThreshold){

        Template localTemplate = template.clone();
        Template localBackground = backgroundTemplate.clone();

        convertTo(localTemplate,convertType);
        convertTo(localBackground,convertType);

        if (localBackground.width() < localTemplate.width() || localBackground.height() < localTemplate.height()) {
            System.err.println("Error: Template dimensions are larger than the background image.");
            return null;
        }

        int resultCols = localBackground.cols() - localTemplate.cols() + 1;
        int resultRows = localBackground.rows() - localTemplate.rows() + 1;
        Mat resultMat = new Mat(resultRows, resultCols, CvType.CV_32FC1);

        matchTemplate(localBackground.mat,localTemplate.mat,resultMat,TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(resultMat);
        // performMatch()
        double bestScore = mmr.maxVal;
        if (bestScore >= confidenceThreshold) {
            // If it is, create and return the MatchResult object.
            Point bestLocation = mmr.maxLoc;
            Rect rect = new Rect(bestLocation, localTemplate.size());
            // --- START OF MODIFICATION ---
            return new MatchResult(rect, bestScore, confidenceThreshold, template.id, backgroundTemplate.id, convertType);
            // --- END OF MODIFICATION ---
        }
        // --- START OF MODIFICATION ---
        return new MatchResult(null, bestScore, confidenceThreshold, template.id, backgroundTemplate.id, convertType);
        // --- END OF MODIFICATION ---
    }


    public static List<MatchResult> findMultipleMatches(Template template, Template backgroundTemplate, MatType convertType){
        return findMultipleMatches(template,backgroundTemplate,convertType,DEFAULT_CONFIDENCE_THRESHOLD, 0.5);
    }

    public static List<MatchResult> findMultipleMatches(Template template, Template backgroundTemplate, MatType convertType, double confidenceThreshold) {
        // Overload with a default overlap threshold
        return findMultipleMatches(template, backgroundTemplate, convertType, confidenceThreshold, 0.5);
    }
    public static List<MatchResult> findMultipleMatches(Template template, Template backgroundTemplate, MatType convertType, double confidenceThreshold, double overlapThreshold) {
        if (template.empty() || backgroundTemplate.empty() || backgroundTemplate.width() < template.width() || backgroundTemplate.height() < template.height()) {
            System.err.println("Error: Invalid input images for findMultipleMatches.");
            return new ArrayList<>(); // Return an empty list on error
        }

        Template localTemplate = template.clone();
        Template localBackground = backgroundTemplate.clone();

        convertTo(localTemplate,convertType);
        convertTo(localBackground,convertType);

        int resultCols = localBackground.cols() - localTemplate.cols() + 1;
        int resultRows = localBackground.rows() - localTemplate.rows() + 1;
        Mat resultMat = new Mat(resultRows, resultCols, CvType.CV_32FC1);
        Imgproc.matchTemplate(localBackground.mat, localTemplate.mat, resultMat, Imgproc.TM_CCOEFF_NORMED);

        List<MatchResult> candidates = new ArrayList<>();
        for (int y = 0; y < resultMat.rows(); y++) {
            for (int x = 0; x < resultMat.cols(); x++) {
                if (resultMat.get(y, x)[0] >= confidenceThreshold) {
                    Point matchLoc = new Point(x, y);
                    Rect rect = new Rect(matchLoc, localTemplate.size());
                    // --- START OF MODIFICATION ---
                    candidates.add(new MatchResult(rect, resultMat.get(y, x)[0], confidenceThreshold, template.id, backgroundTemplate.id, convertType));
                    // --- END OF MODIFICATION ---
                }
            }
        }

        System.out.printf("Found %d raw matches above the threshold of %.2f.%n", candidates.size(), confidenceThreshold);
        if (candidates.isEmpty()) {
            return candidates;
        }

        // Non-Maximal Suppression
        candidates.sort(Comparator.comparing(MatchResult::getScore).reversed());
        List<MatchResult> winners = new ArrayList<>();

        while(!candidates.isEmpty()) {
            MatchResult champion = candidates.getFirst();
            winners.add(champion);
            candidates.removeIf(competitor -> {
                double iou = calculateIntersectionOverUnion(champion.rectLocation, competitor.rectLocation);
                return iou > overlapThreshold;
            });
        }
        System.out.printf("Returning %d non-overlapping matches.%n", winners.size());
        return winners;
    }

    private static double calculateIntersectionOverUnion(Rect r1, Rect r2) {
        int xA = Math.max(r1.x, r2.x);
        int yA = Math.max(r1.y, r2.y);
        int xB = Math.min(r1.x + r1.width, r2.x + r2.width);
        int yB = Math.min(r1.y + r1.height, r2.y + r2.height);

        int intersectionArea = Math.max(0, xB - xA) * Math.max(0, yB - yA);

        int r1Area = r1.width * r1.height;
        int r2Area = r2.width * r2.height;

        double unionArea = (double) r1Area + r2Area - intersectionArea;

        return intersectionArea / unionArea;
    }
    public static List<MatchResult> findBestMatchPerTemplate(List<Template> allTemplates, Template backgroundTemplate, MatType convertType){
        return findBestMatchPerTemplate(allTemplates,backgroundTemplate,convertType,DEFAULT_CONFIDENCE_THRESHOLD);
    }
    public static List<MatchResult> findBestMatchPerTemplate(List<Template> allTemplates, Template backgroundTemplate, MatType convertType, double confidenceThreshold){
        return allTemplates
                .parallelStream()
                .map(template -> findBestMatch(template,backgroundTemplate,convertType,confidenceThreshold))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static MatchResult findOneTemplate(List<Template> allTemplates, Template backgroundTemplate,MatType convertType){
        return findOneTemplate(allTemplates,backgroundTemplate,convertType,DEFAULT_CONFIDENCE_THRESHOLD);
    }
    public static MatchResult findOneTemplate(List<Template> allTemplates, Template backgroundTemplate,MatType convertType, double confidenceThreshold){
        return allTemplates
                .parallelStream()
                .map(template -> findBestMatch(template, backgroundTemplate, convertType, confidenceThreshold))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }


    public static MatchResult findBestOneTemplate(List<Template> allTemplates, Template backgroundTemplate,MatType convertType) {
        return findBestOneTemplate(allTemplates,backgroundTemplate,convertType,DEFAULT_CONFIDENCE_THRESHOLD);
    }

    public static MatchResult findBestOneTemplate(List<Template> allTemplates, Template backgroundTemplate,MatType convertType, double confidenceThreshold){
        return findBestMatchPerTemplate(allTemplates,backgroundTemplate,convertType,confidenceThreshold)
                .stream()
                .max(Comparator.comparing(MatchResult::getScore))
                .orElse(null);
    }

    public static List<MatchResult> findAllMatches(List<Template> allTemplates, Template backgroundTemplate,MatType convertType, double confidenceThreshold){
        return allTemplates
                .parallelStream()
                .flatMap(template ->
                        Objects.requireNonNull(findMultipleMatches(template, backgroundTemplate, convertType, confidenceThreshold)).stream()
                )
                .collect(Collectors.toList());
    }

    public static List<MatchResult> findAllMatches(List<Template> allTemplates, List<Template> allBackgrounds,MatType convertType, double confidenceThreshold){
        return allBackgrounds
                .parallelStream()
                .flatMap(background -> findAllMatches(allTemplates, background, convertType, confidenceThreshold).stream())
                .collect(Collectors.toList());
    }


    public static MatchResult findBestInBackgrounds(
            Template template,
            List<Template> allBackgrounds, // Changed to List<Mat> for clarity
            MatType matType,
            double confidenceThreshold)
    {
        return allBackgrounds
                .parallelStream()
                .map(backgroundMat -> findBestMatch(template, backgroundMat, matType, confidenceThreshold))
                .filter(Objects::nonNull)
                .max(Comparator.comparing(MatchResult::getScore))
                .orElse(null);
    }


    public static List<MatchResult> findBestPerBackground(
            Template template,
            List<Template> allBackgrounds, // Changed to List<Mat>
            MatType matType,
            double confidenceThreshold)
    {
        // This implementation is already correct for its stated goal.
        return allBackgrounds
                .parallelStream()
                .map(backgroundMat -> findBestMatch(template, backgroundMat, matType, confidenceThreshold))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    //Competitive Matches

    private static boolean hasConflictWith(MatchResult wantedMatch,
                                           Template badTemplate,
                                           Template backgroundTemplate,
                                           MatType matType,
                                           double confidenceThreshold) {

        // If the wanted match wasn't even found, there can't be a conflict.
        if (wantedMatch == null || wantedMatch.rectLocation == null) {
            return false;
        }

        MatchResult badMatch = findBestMatch(badTemplate, backgroundTemplate, matType, confidenceThreshold);

        // If the bad template wasn't found or the matches don't intersect, there's no conflict.
        if (badMatch == null || badMatch.rectLocation == null || !intersects(wantedMatch.rectLocation, badMatch.rectLocation)) {
            return false;
        }

        // A conflict only exists if the bad match is found at the same location with an equal or higher score.
        if (badMatch.getScore() >= wantedMatch.getScore()) {
            System.out.printf(
                    "Conflict found: Wanted '%s' (Score: %.4f) was challenged by '%s' (Score: %.4f) at the same location.%n",
                    wantedMatch.winningTemplateId, wantedMatch.getScore(), badMatch.winningTemplateId, badMatch.getScore()
            );
            return true;
        }

        return false;
    }


    public static List<MatchResult> findCompetitiveMatches(
            List<Template> allTemplates,
            Template background,
            MatType matType,
            double confidenceThreshold)
    {
        List<MatchResult> candidates = findAllMatches(allTemplates, background, matType, confidenceThreshold);
        candidates.sort(Comparator.comparing(MatchResult::getScore).reversed());
        List<MatchResult> winners = new ArrayList<>();
        while (!candidates.isEmpty()) {
            MatchResult champion = candidates.getFirst();
            winners.add(champion);
            candidates.removeIf(competitor ->
                    intersects(champion.rectLocation, competitor.rectLocation)
            );
        }
        return winners;
    }

    public static MatchResult findWantedTemplate(
            Template wantedTemplate,
            Template backgroundTemplate,
            Template badTemplate,
            MatType matType,
            double confidenceThreshold) {
        MatchResult wantedMatch = findBestMatch(wantedTemplate, backgroundTemplate, matType, confidenceThreshold);
        if (wantedMatch == null) {
            return null;
        }
        // Step 2: Delegate the conflict check to our new helper function.
        if (hasConflictWith(wantedMatch, badTemplate, backgroundTemplate, matType, confidenceThreshold)) {
            return null; // A conflict was found, so the match is invalid.
        }

        // No conflict, the match is valid.
        return wantedMatch;
    }

    public static MatchResult findWantedTemplate(
            Template wantedTemplate,
            Template backgroundTemplate,
            List<Template> allBadTemplates,
            MatType matType,
            double confidenceThreshold)
    {
        MatchResult wantedMatch = findBestMatch(wantedTemplate, backgroundTemplate, matType, confidenceThreshold);
        if (wantedMatch == null) {
            return null;
        }

        // Step 2: Use a parallel stream to see if ANY bad template causes a conflict.
        // The lambda becomes a simple and clean call to our helper function.
        boolean hasAnyConflict = allBadTemplates
                .parallelStream()
                .anyMatch(badTemplate -> hasConflictWith(wantedMatch, badTemplate, backgroundTemplate, matType, confidenceThreshold));

        // Step 3: Return the result.
        if (hasAnyConflict) {
            return null; // At least one conflict was found, so the match is invalid.
        } else {
            return wantedMatch; // No conflicts found, the match is valid.
        }
    }

    public static List<MatchResult> findEachWantedTemplate(
            List<Template> allWantedTemplates,
            Template backgroundTemplate,
            Template badTemplate,
            MatType matType,
            double confidenceThreshold)
    {
        Template localBackground = backgroundTemplate.clone();
        Template localBadTemplate = badTemplate.clone();

        return allWantedTemplates
                .parallelStream()
                .map(wantedTemplate ->
                        findWantedTemplate(wantedTemplate.clone(), localBackground, localBadTemplate, matType, confidenceThreshold)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    public static List<MatchResult> findEachWantedTemplate(
            List<Template> allWantedTemplates,
            Template backgroundTemplate,
            List<Template> allBadTemplates,
            MatType matType,
            double confidenceThreshold)
    {
        return allWantedTemplates
                .parallelStream()
                .map(wantedTemplate -> {
                    Template localWanted = wantedTemplate.clone();
                    return findWantedTemplate(localWanted, backgroundTemplate, allBadTemplates, matType, confidenceThreshold);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }



    private static boolean intersects(Rect r1, Rect r2) {
        return r1.x < r2.x + r2.width &&
                r1.x + r1.width > r2.x &&
                r1.y < r2.y + r2.height &&
                r1.y + r1.height > r2.y;
    }
}