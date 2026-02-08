package com.example.punchclock.ui.chart;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer {

    private float mRadius;

    public RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, float radius) {
        super(chart, animator, viewPortHandler);
        this.mRadius = radius;
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(com.github.mikephil.charting.utils.Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // initialize the buffer
        if (mBarBuffers != null) {
            BarBuffer buffer = mBarBuffers[index];
            buffer.setPhases(phaseX, phaseY);
            buffer.setDataSet(index);
            buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
            buffer.setBarWidth(mChart.getBarData().getBarWidth());

            buffer.feed(dataSet);

            trans.pointValuesToPixel(buffer.buffer);

            final boolean isSingleColor = dataSet.getColors().size() == 1;

            if (isSingleColor) {
                mRenderPaint.setColor(dataSet.getColor());
            }

            for (int j = 0; j < buffer.buffer.length; j += 4) {

                if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                    break;

                if (!isSingleColor) {
                    mRenderPaint.setColor(dataSet.getColor(j / 4));
                }

                float left = buffer.buffer[j];
                float top = buffer.buffer[j + 1];
                float right = buffer.buffer[j + 2];
                float bottom = buffer.buffer[j + 3];

                // Ensure top is always smaller than bottom for RectF (coordinate system)
                // In chart, top value is smaller y pixel? No, usually chart bottom is higher y pixel.
                // MPAndroidChart handles buffer calculation.
                
                RectF barRect = new RectF(left, top, right, bottom);
                c.drawRoundRect(barRect, mRadius, mRadius, mRenderPaint);

                if (drawBorder) {
                    c.drawRoundRect(barRect, mRadius, mRadius, mBarBorderPaint);
                }
            }
        }
    }
}