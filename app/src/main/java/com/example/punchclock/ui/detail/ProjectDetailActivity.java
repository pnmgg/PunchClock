package com.example.punchclock.ui.detail;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.punchclock.R;

import com.example.punchclock.data.Project;
import com.example.punchclock.data.PunchRecord;
import com.example.punchclock.viewmodel.PunchViewModel;
import com.example.punchclock.ui.chart.AnalysisUtils;
import com.example.punchclock.ui.chart.CustomMarkerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PROJECT_ID = "com.example.punchclock.EXTRA_PROJECT_ID";
    public static final String EXTRA_PROJECT_NAME = "com.example.punchclock.EXTRA_PROJECT_NAME";

    private PunchViewModel punchViewModel;
    private long projectId;
    private String projectName;
    private YearMonth currentMonth;
    private CalendarAdapter calendarAdapter;
    private TextView monthYearText;
    private Project currentProject;
    
    // Stats
    private BarChart barChart;
    private TextView statsSummaryText;
    private ImageButton btnExportChart;
    private List<PunchRecord> allRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        projectId = getIntent().getLongExtra(EXTRA_PROJECT_ID, -1);
        projectName = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
        
        if (projectId == -1) {
            finish();
            return;
        }

        setTitle(projectName);
        
        currentMonth = YearMonth.now();
        
        initViews();
        initViewModel();
    }

    private void initViews() {
        monthYearText = findViewById(R.id.text_month_year);
        ImageButton btnPrev = findViewById(R.id.btn_prev_month);
        ImageButton btnNext = findViewById(R.id.btn_next_month);
        RecyclerView recyclerView = findViewById(R.id.calendar_recycler_view);
        barChart = findViewById(R.id.chart_stats);
        statsSummaryText = findViewById(R.id.text_stats_summary);
        btnExportChart = findViewById(R.id.btn_chart_export);

        btnPrev.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });
        
        btnExportChart.setOnClickListener(v -> exportChart());

        calendarAdapter = new CalendarAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        recyclerView.setAdapter(calendarAdapter);
        
        calendarAdapter.setOnDateClickListener((date, isPunched) -> {
            if (isPunched) {
                punchViewModel.deleteRecord(projectId, date.toEpochDay());
            } else {
                punchViewModel.insertRecord(new PunchRecord(projectId, date.toEpochDay()));
            }
        });

        // Date Picker for Quick Jump
        monthYearText.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                currentMonth = YearMonth.of(year, month + 1); // DatePicker month is 0-indexed
                updateCalendar();
            }, currentMonth.getYear(), currentMonth.getMonthValue() - 1, 1);
            
            dialog.getDatePicker().setCalendarViewShown(false);
            dialog.setTitle("选择月份");
            dialog.show();
        });

        // Gesture Navigation
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) {
                        // Swipe Right -> Prev Month
                        currentMonth = currentMonth.minusMonths(1);
                        updateCalendar();
                        return true;
                    } else {
                        // Swipe Left -> Next Month
                        currentMonth = currentMonth.plusMonths(1);
                        updateCalendar();
                        return true;
                    }
                }
                return false;
            }
        });

        // Attach gesture listener to RecyclerView
        recyclerView.setOnTouchListener((v, event) -> {
             gestureDetector.onTouchEvent(event);
             return false; 
        });
        
        // Setup Chart Interaction
        setupChart();
    }
    
    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        
        // Intelligent Analysis: Add interaction listener
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // Could show a toast or update text details, but MarkerView handles display
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    private void exportChart() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return;
            }
        }
        
        try {
            if (barChart.saveToGallery("chart_" + projectId + "_" + System.currentTimeMillis(), 85)) {
                Toast.makeText(this, "图表已保存到相册", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                exportChart();
            } else {
                Toast.makeText(this, "需要存储权限才能保存图表", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViewModel() {
        punchViewModel = new ViewModelProvider(this).get(PunchViewModel.class);
        
        // Load project details (mainly for color)
        punchViewModel.getAllProjects().observe(this, projects -> {
            if (projects == null) return;
            for (Project p : projects) {
                if (p.id == projectId) {
                    currentProject = p;
                    updateCalendar(); // To apply color
                    break;
                }
            }
        });

        punchViewModel.getRecordsForProject(projectId).observe(this, records -> {
            if (records == null) {
                records = new ArrayList<>();
            }
            allRecords = records;
            List<Long> punchedDates = records.stream()
                    .map(r -> r.date)
                    .collect(Collectors.toList());
            
            if (currentProject != null) {
                calendarAdapter.setPunchedDates(punchedDates, currentProject.color);
            } else {
                 calendarAdapter.setPunchedDates(punchedDates, android.graphics.Color.GRAY);
            }
            updateStats(records);
        });

        updateCalendar();
    }

    private void updateCalendar() {
        monthYearText.setText(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年 MM月", Locale.CHINA)));
        
        List<LocalDate> days = new ArrayList<>();
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        
        // Add empty days for padding (assuming Monday start)
        for (int i = 1; i < dayOfWeek; i++) {
            days.add(null);
        }
        
        for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
            days.add(currentMonth.atDay(i));
        }
        
        calendarAdapter.setDays(days);
        
        // Ensure chart updates when month changes
        if (allRecords != null) {
            updateStats(allRecords);
        }
    }
    
    private void updateStats(List<PunchRecord> records) {
        if (records == null || records.isEmpty()) {
            barChart.clear();
            statsSummaryText.setText("暂无打卡记录");
            return;
        }

        // 1. Calculate stats by month (showing 6 months ending with currentMonth)
        Map<YearMonth, Integer> monthlyCounts = new HashMap<>();
        // Use currentMonth from calendar navigation as the end point
        YearMonth endMonth = currentMonth; 
        YearMonth startMonth = endMonth.minusMonths(5);
        
        for (PunchRecord r : records) {
            LocalDate date = LocalDate.ofEpochDay(r.date);
            YearMonth ym = YearMonth.from(date);
            if (!ym.isBefore(startMonth) && !ym.isAfter(endMonth)) {
                monthlyCounts.put(ym, monthlyCounts.getOrDefault(ym, 0) + 1);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < 6; i++) {
            YearMonth ym = startMonth.plusMonths(i);
            int count = monthlyCounts.getOrDefault(ym, 0);
            entries.add(new BarEntry(i, count));
            labels.add(ym.format(DateTimeFormatter.ofPattern("MM")));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Punches");
        if (currentProject != null) {
            dataSet.setColor(currentProject.color);
        }
        
        // Intelligent Analysis: Add LimitLine for Average
        float avg = AnalysisUtils.calculateAverage(entries);
        LimitLine ll = new LimitLine(avg, "平均值: " + String.format("%.1f", avg));
        ll.setLineColor(Color.parseColor("#9DA8A4")); // Morandi Sage Green
        ll.setLineWidth(2f);
        ll.enableDashedLine(15f, 10f, 0f); // Dashed line for softness
        ll.setTextColor(Color.parseColor("#5E616D")); // Morandi Text
        ll.setTextSize(10f);
        
        barChart.getAxisLeft().removeAllLimitLines();
        barChart.getAxisLeft().addLimitLine(ll);

        // Intelligent Analysis: Highlight outliers
        List<Integer> outlierIndices = AnalysisUtils.findOutliers(entries);
        // Note: MPAndroidChart doesn't support individual bar highlighting persistently easily without custom renderer or multiple datasets
        // But we can change color of specific entries if we really want, or just rely on MarkerView
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        
        barChart.setData(barData);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        
        // Interaction: Add MarkerView
        CustomMarkerView mv = new CustomMarkerView(this, R.layout.custom_marker_view, labels);
        mv.setChartView(barChart);
        barChart.setMarker(mv);
        
        barChart.animateY(1000); // Animation
        barChart.invalidate(); // refresh

        // 2. Calculate summary stats
        int currentMonthCount = 0;
        int currentYearCount = 0;
        
        int targetYear = currentMonth.getYear();
        int targetMonth = currentMonth.getMonthValue();
        
        // Track which months have punches in the current year
        Set<Integer> activeMonths = new java.util.HashSet<>();
        
        for (PunchRecord r : records) {
            LocalDate date = LocalDate.ofEpochDay(r.date);
            if (date.getYear() == targetYear) {
                currentYearCount++;
                activeMonths.add(date.getMonthValue());
                if (date.getMonthValue() == targetMonth) {
                    currentMonthCount++;
                }
            }
        }
        
        // Calculate average only based on months that have punches
        double yearMonthlyAvg = 0;
        if (!activeMonths.isEmpty()) {
            yearMonthlyAvg = (double) currentYearCount / activeMonths.size();
        }

        String summary = String.format("本月打卡数: %d 次\n本年度打卡数: %d 次\n本年度月均: %.1f 次", 
                currentMonthCount, currentYearCount, yearMonthlyAvg);
        statsSummaryText.setText(summary);
    }
}
