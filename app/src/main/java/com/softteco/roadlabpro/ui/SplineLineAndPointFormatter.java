package com.softteco.roadlabpro.ui;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.androidplot.ui.SeriesRenderer;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.BezierLineAndPointFormatter;
import com.androidplot.xy.FillDirection;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

public class SplineLineAndPointFormatter extends LineAndPointFormatter {

    public SplineLineAndPointFormatter() { }

    public SplineLineAndPointFormatter(Integer lineColor, Integer vertexColor, Integer fillColor) {
        super(lineColor, vertexColor, fillColor, null);
    }

    public SplineLineAndPointFormatter(Integer lineColor, Integer vertexColor, Integer fillColor, FillDirection fillDir) {
        super(lineColor, vertexColor, fillColor, null, fillDir);
    }

    @Override
    public Class<? extends SeriesRenderer> getRendererClass() {
        return SplineLineAndPointRenderer.class;
    }

    @Override
    public SeriesRenderer getRendererInstance(XYPlot plot) {
        return new SplineLineAndPointRenderer(plot);
    }

    public static class SplineLineAndPointRenderer extends LineAndPointRenderer<BezierLineAndPointFormatter> {

        static class Point {
            public float x, y, dx, dy;
            public Point(PointF pf) { x = pf.x; y = pf.y; }
        }

        private Point prev, point, next;
        private int pointsCounter;

        public SplineLineAndPointRenderer(XYPlot plot) {
            super(plot);
        }

        @Override
        protected void appendToPath(Path path, final PointF thisPoint, PointF lastPoint) {
            pointsCounter--;
            if (point == null) {
                point = new Point(thisPoint);
                point.dx = ((point.x - prev.x) / 5);
                point.dy = ((point.y - prev.y) / 5);
                return;
            } else if (next == null) {
                next = new Point(thisPoint);
            } else {
                prev = point;
                point = next;
                next = new Point(thisPoint);
            }
            point.dx = ((next.x - prev.x) / 5);
            point.dy = ((next.y - prev.y) / 5);
            path.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
            if (pointsCounter == 1) { // last point
                next.dx = ((next.x - point.x) / 5);
                next.dy = ((next.y - point.y) / 5);
                path.cubicTo(point.x + point.dx, point.y + point.dy, next.x - next.dx, next.y - next.dy, next.x, next.y);
            }
        }

        @Override
        protected void drawSeries(Canvas canvas, RectF plotArea, XYSeries series, LineAndPointFormatter formatter) {
            Number x = null;
            Number y = null;
            if (series != null && series.size() > 0) {
                y = series.getY(0);
                x = series.getX(0);
            }
            if (x == null || y == null) {
                return;
            }
            XYPlot p = getPlot();
            PointF thisPoint = ValPixConverter.valToPix(x, y, plotArea,
            p.getCalculatedMinX(), p.getCalculatedMaxX(), p.getCalculatedMinY(), p.getCalculatedMaxY());
            prev = new Point(thisPoint);
            point = next = null;
            pointsCounter = series.size();
            super.drawSeries(canvas, plotArea, series, formatter);
        }
    }
}
