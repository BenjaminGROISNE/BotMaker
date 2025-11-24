package com.botmaker.library.opencv;

import org.opencv.core.Rect;


public class MatchResult {
    public Rect rectLocation;
    public double score;
    public double confidenceThreshold;
    public String winningTemplateId;
    public String winningBackgroundId;
    // --- START OF MODIFICATION ---
    public final MatType matType;

    public MatchResult(Rect rect, double score, double threshold, String winningTemplateId, String winningBackgroundId, MatType matType) {
        this.rectLocation = rect;
        this.score = score;
        this.confidenceThreshold = threshold;
        this.winningTemplateId = winningTemplateId;
        this.winningBackgroundId = winningBackgroundId;
        this.matType = matType;
    }

    private MatchResult() {
        this.rectLocation = new Rect();
        this.score = 0;
        this.confidenceThreshold = 0;
        this.winningTemplateId = null;
        this.winningBackgroundId = null;
        this.matType = null;
    }

    public static MatchResult noMatch() {
        return new MatchResult();
    }

    public String getTemplateId() {
        return winningTemplateId;
    }
    public String getBackgroundId() {
        return winningBackgroundId;
    }

    public double getScore(){
        return score;
    }

    public Boolean isMatch(){
        if (winningTemplateId == null) {
            return false;
        }
        return score>=confidenceThreshold;
    }

    @Override
    public String toString() {
        if (winningTemplateId == null) {
            return "MatchResult [No Match]";
        }
        // --- START OF MODIFICATION ---
        return String.format("MatchResult [Template: %s, Background: %s, Score: %.4f, Location: %s, Threshold: %.4f, MatType: %s, IsSignificant: %b]",
                winningTemplateId != null ? winningTemplateId : "N/A",
                winningBackgroundId != null ? winningBackgroundId : "N/A",
                score,
                rectLocation != null ? rectLocation.toString() : "N/A",
                confidenceThreshold,
                matType,
                isMatch());
    }
}