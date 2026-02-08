package com.example.punchclock.ui.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.mikephil.charting.data.BarEntry;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AnalysisUtilsTest {

    @Test
    public void testCalculateAverage() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 10));
        entries.add(new BarEntry(1, 20));
        entries.add(new BarEntry(2, 30));

        float avg = AnalysisUtils.calculateAverage(entries);
        assertEquals(20.0f, avg, 0.001f);
    }

    @Test
    public void testFindOutliers() {
        List<BarEntry> entries = new ArrayList<>();
        // Mean = 10, StdDev approx 0
        entries.add(new BarEntry(0, 10));
        entries.add(new BarEntry(1, 10));
        entries.add(new BarEntry(2, 10));
        entries.add(new BarEntry(3, 10));
        
        // Outlier
        entries.add(new BarEntry(4, 100)); // Huge jump

        List<Integer> outliers = AnalysisUtils.findOutliers(entries);
        // The mean will be shifted, but 100 should definitely be > Mean + 1.5 * StdDev
        assertTrue(outliers.contains(4));
    }
}
