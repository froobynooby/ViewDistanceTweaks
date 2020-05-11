package com.froobworld.viewdistancetweaks.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a tool for finding the total area of a set of integer rectangles where intersections are not double counted.
 */
public class RectangleUnionAreaFinder {
    private final List<Rect> disjointRectSet = new ArrayList<>();
    private int area = 0;

    public RectangleUnionAreaFinder addRect(int x0, int y0, int x1, int y1) {
        Rect addingRect = new Rect(x0, y0, x1, y1);
        List<Rect> newRects = new ArrayList<>();
        List<Rect> newNewRects = new ArrayList<>();
        newRects.add(addingRect);
        for (Rect rect : disjointRectSet) {
            if (addingRect.xMax <= rect.xMin || addingRect.xMin >= rect.xMax
                    ||  addingRect.yMax <= rect.yMin || addingRect.yMin >= rect.yMax) {
                continue;
            }
            newNewRects.clear();
            for (Rect newRect : newRects) {
                Rect[] rects = newRect.getRectDifference(rect);
                for (Rect value : rects) {
                    if (value != null) {
                        newNewRects.add(value);
                    }
                }
            }
            List<Rect> oldNewRects = newRects;
            newRects = newNewRects;
            newNewRects = oldNewRects;

        }
        for (Rect rect : newRects) {
            area += rect.area();
        }
        disjointRectSet.addAll(newRects);
        return this;
    }

    public int area() {
        return area;
    }

    private static class Rect {
        public final int xMax, yMax, xMin, yMin;

        private Rect(int x0, int y0, int x1, int y1) {
            this.xMax = Math.max(x0, x1);
            this.yMax = Math.max(y0, y1);
            this.xMin = Math.min(x0, x1);
            this.yMin = Math.min(y0, y1);
        }


        public int area() {
            return (xMax - xMin) * (yMax - yMin);
        }

        // Returns up to four disjoint rectangles that are disjoint from otherRect, and whose union is the difference of this and otherRect
        public Rect[] getRectDifference(Rect otherRect) {
            // Case: not overlapping
            if (this.xMax <= otherRect.xMin || this.xMin >= otherRect.xMax
                    ||  this.yMax <= otherRect.yMin || this.yMin >= otherRect.yMax) {
                return new Rect[]{this};
            }
            Rect[] rects = new Rect[4];

            // Rect 0 : left side
            {
                int xMin = this.xMin;
                int xMax = otherRect.xMin;
                int yMin = this.yMin;
                int yMax = this.yMax;
                if (xMin < xMax && yMin < yMax) {
                    rects[0] = new Rect(xMin, yMin, xMax, yMax);
                }
            }
            // Rect 1 : top
            {
                int xMin = Math.max(otherRect.xMin, this.xMin);
                int xMax = Math.min(otherRect.xMax, this.xMax);
                int yMin = otherRect.yMax;
                int yMax = this.yMax;
                if (xMin < xMax && yMin < yMax) {
                    rects[1] = new Rect(xMin, yMin, xMax, yMax);
                }
            }
            // Rect 2 : right side
            {
                int xMin = otherRect.xMax;
                int xMax = this.xMax;
                int yMin = this.yMin;
                int yMax = this.yMax;
                if (xMin < xMax && yMin < yMax) {
                    rects[2] = new Rect(xMin, yMin, xMax, yMax);
                }
            }
            // Rect 3 : bottom
            {
                int xMin = Math.max(otherRect.xMin, this.xMin);
                int xMax = Math.min(otherRect.xMax, this.xMax);
                int yMin = this.yMin;
                int yMax = otherRect.yMin;
                if (xMin < xMax && yMin < yMax) {
                    rects[3] = new Rect(xMin, yMin, xMax, yMax);
                }
            }
            return rects;
        }

    }

}
