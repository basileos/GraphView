package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;

/**
 * Created by V.Dorozhynskiy on 18.07.13.
 * Special chart view, which consists of main line chart and secondary bar chart
 * IMPORTANT we assume that LINE chart is the main part and the bar chart is utility part. Their data should not be messed in the chart data processing
 */
public class CombinedGraphView extends LineGraphView {

    public CombinedGraphView(Context context, String title) {
        super(context, title);
        mainChartHeightPortion = 0.8f;
    }


    @Override
    public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeries.GraphViewSeriesStyle style) {
        if (chartWidthRatio != 0) {
            graphwidth = (float) (graphwidth * chartWidthRatio);
        }

        switch (style.getChartStyle()) {
            case LINE:
                // draw data
                paint.setStrokeWidth(style.thickness);
                paint.setColor(style.color);

                double lastEndY = 0;
                double lastEndX = 0;

                for (int i = 0; i < values.length; i++) {
                    double valY = values[i].valueY - minY;
                    double ratY = valY / diffY;
                    double y = graphheight * ratY;

                    double valX = values[i].valueX - minX;
                    double ratX = valX / diffX;
                    double x = graphwidth * ratX;

                    if (i > 0) {
                        float startX = (float) lastEndX + (horstart + 1);
                        float startY = (float) (border - lastEndY) + graphheight;
                        float endX = (float) x + (horstart + 1);
                        float endY = (float) (border - y) + graphheight;

                        canvas.drawLine(startX, startY, endX, endY, paint);
                    }
                    lastEndY = y;
                    lastEndX = x;
                }
                break;

            case BAR:
                float colwidth = graphwidth / values.length;

                paint.setStrokeWidth(style.thickness);
                paint.setColor(style.color);

                //secondary chart tuning
                minY = getMinY(false);
                diffY = getMaxY(false) - minY;
                float utilityGraphHeight = graphheight - (graphheight * mainChartHeightPortion) + border;

                // draw data
                float bottom = graphheight + utilityGraphHeight + border;

                for (int i = 0; i < values.length; i++) {
                    float valY = (float) (values[i].valueY - minY);
                    float y = utilityGraphHeight * (float)(valY / diffY);

                    float left = (i * colwidth) + horstart;
                    float top = graphheight + utilityGraphHeight + border - y - 1;
                    float right = left + colwidth - 1;

                    canvas.drawRect(left, top, right, bottom, paint);
                }
                break;
        }
    }

    @Override
    protected double getMinY() {
        return getMinY(true);
    }

    private double getMinY(boolean forMainChart) {
        double smallest;
        if (manualYAxis) {
            smallest = manualMinYValue;
        } else {
            smallest = Integer.MAX_VALUE;
            for (int i=0; i<graphSeries.size(); i++) {
                if (!isActualDataSerie(graphSeries.get(i), forMainChart)) {
                    continue;
                }
                GraphViewData[] values = _values(i);
                for (int ii=0; ii<values.length; ii++)
                    if (values[ii].valueY < smallest)
                        smallest = values[ii].valueY;
            }
        }
        return smallest;
    }

    @Override
    protected double getMaxY() {
        return getMaxY(true);
    }

    private double getMaxY(boolean forMainChart) {
        double largest;
        if (manualYAxis) {
            largest = manualMaxYValue;
        } else {
            largest = Integer.MIN_VALUE;
            for (int i=0; i<graphSeries.size(); i++) {
                if (!isActualDataSerie(graphSeries.get(i), forMainChart)) {
                    continue;
                }
                GraphViewData[] values = _values(i);
                for (int ii=0; ii<values.length; ii++)
                    if (values[ii].valueY > largest)
                        largest = values[ii].valueY;
            }
        }
        return largest;
    }

    @Override
    protected double getMinX(boolean ignoreViewport) {
        return getMinX(ignoreViewport, true);
    }

    private double getMinX(boolean ignoreViewport, boolean isMainChart) {
        // if viewport is set, use this
        if (!ignoreViewport && viewportSize != 0) {
            return viewportStart;
        } else {
            // otherwise use the min x value
            // values must be sorted by x, so the first value has the smallest X value
            double lowest = 0;
            if (graphSeries.size() > 0) {
                GraphViewData[] values = graphSeries.get(0).values;
                if (values.length == 0 && isActualDataSerie(graphSeries.get(0), isMainChart)) {
                    lowest = 0;
                } else {
                    lowest = values[0].valueX;
                    for (int i=1; i<graphSeries.size(); i++) {
                        if (!isActualDataSerie(graphSeries.get(i), isMainChart)) {
                            continue;
                        }
                        values = graphSeries.get(i).values;
                        lowest = Math.min(lowest, values[0].valueX);
                    }
                }
            }
            return lowest;
        }
    }

    @Override
    protected double getMaxX(boolean ignoreViewport) {
        return getMaxX(ignoreViewport, true);
    }

    private double getMaxX(boolean ignoreViewport, boolean isMainChart) {
        // if viewport is set, use this
        if (!ignoreViewport && viewportSize != 0) {
            return viewportStart+viewportSize;
        } else {
            // otherwise use the max x value
            // values must be sorted by x, so the last value has the largest X value
            double highest = 0;
            if (graphSeries.size() > 0)
            {
                GraphViewData[] values = graphSeries.get(0).values;
                if (values.length == 0 && isActualDataSerie(graphSeries.get(0), isMainChart)) {
                    highest = 0;
                } else {
                    highest = values[values.length-1].valueX;
                    for (int i=1; i<graphSeries.size(); i++) {
                        if (!isActualDataSerie(graphSeries.get(i), isMainChart)) {
                            continue;
                        }
                        values = graphSeries.get(i).values;
                        highest = Math.max(highest, values[values.length-1].valueX);
                    }
                }
            }
            return highest;
        }
    }

    private boolean isActualDataSerie(GraphViewSeries serie, boolean isMainChart) {
        return (isMainChart && serie.style.getChartStyle().equals(GraphViewSeries.ChartStyle.LINE)) || (!isMainChart && serie.style.getChartStyle().equals(GraphViewSeries.ChartStyle.BAR));
    }
}
