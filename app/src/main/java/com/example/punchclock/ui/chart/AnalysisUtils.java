package com.example.punchclock.ui.chart;

import android.graphics.Color;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class AnalysisUtils {

    /**
     * Calculate average value from a list of BarEntries.
     */
    public static float calculateAverage(List<BarEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;
        float sum = 0;
        for (BarEntry e : entries) {
            sum += e.getY();
        }
        return sum / entries.size();
    }

    /**
     * Identify outliers using a simple method (e.g., > 2 * Mean).
     * Returns a list of indices that are outliers.
     */
    public static List<Integer> findOutliers(List<BarEntry> entries) {
        List<Integer> outliers = new ArrayList<>();
        if (entries == null || entries.isEmpty()) return outliers;
        
        float avg = calculateAverage(entries);
        // Standard Deviation
        float sumSq = 0;
        for (BarEntry e : entries) {
            sumSq += Math.pow(e.getY() - avg, 2);
        }
        double stdDev = Math.sqrt(sumSq / entries.size());
        
        // Threshold: Mean + 2 * StdDev (or just Mean * 1.5 for simple logic)
        // Let's use Mean + 1.5 * StdDev as threshold for "High Performance"
        double threshold = avg + 1.5 * stdDev;
        
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getY() > threshold) {
                outliers.add(i);
            }
        }
        return outliers;
    }

    /**
     * Suggest a color for the chart based on the base color.
     * Returns a high contrast color (e.g., complementary or darker shade).
     */
    public static int getHighContrastColor(int baseColor) {
        // Simple logic: return a darker version of the base color
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 0.6f; // Make it darker
        return Color.HSVToColor(hsv);
    }
}
