package com.example.punchclock.ui.detail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simple test to verify the logic we put in updateStats, but extracted here since updateStats is private and Android dependent.
// We are testing the logic logic of grouping by YearMonth
public class ChartLogicTest {

    @Test
    public void testChartDataGrouping() {
        // Setup mock data
        List<Long> dates = new ArrayList<>();
        // 2026-01-15
        dates.add(LocalDate.of(2026, 1, 15).toEpochDay());
        // 2026-01-20
        dates.add(LocalDate.of(2026, 1, 20).toEpochDay());
        // 2025-12-31
        dates.add(LocalDate.of(2025, 12, 31).toEpochDay());
        // 2025-08-01
        dates.add(LocalDate.of(2025, 8, 1).toEpochDay());

        YearMonth endMonth = YearMonth.of(2026, 1);
        YearMonth startMonth = endMonth.minusMonths(5); // Aug 2025

        Map<YearMonth, Integer> monthlyCounts = new HashMap<>();

        for (Long d : dates) {
            LocalDate date = LocalDate.ofEpochDay(d);
            YearMonth ym = YearMonth.from(date);
            if (!ym.isBefore(startMonth) && !ym.isAfter(endMonth)) {
                monthlyCounts.put(ym, monthlyCounts.getOrDefault(ym, 0) + 1);
            }
        }

        // Verify
        assertEquals(2, (int) monthlyCounts.getOrDefault(YearMonth.of(2026, 1), 0));
        assertEquals(1, (int) monthlyCounts.getOrDefault(YearMonth.of(2025, 12), 0));
        assertEquals(1, (int) monthlyCounts.getOrDefault(YearMonth.of(2025, 8), 0));
        assertEquals(0, (int) monthlyCounts.getOrDefault(YearMonth.of(2025, 11), 0));
    }
}
